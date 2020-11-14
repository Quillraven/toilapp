package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.ToiletDoesNotExistException
import com.github.quillraven.toilapp.model.db.Toilet
import com.github.quillraven.toilapp.model.dto.CommentDto
import com.github.quillraven.toilapp.model.dto.CreateUpdateCommentDto
import com.github.quillraven.toilapp.model.dto.CreateUpdateRatingDto
import com.github.quillraven.toilapp.model.dto.CreateUpdateToiletDto
import com.github.quillraven.toilapp.model.dto.ToiletDto
import com.github.quillraven.toilapp.repository.ToiletRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Metrics
import org.springframework.data.geo.Point
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ToiletService {
    fun create(createUpdateToiletDto: CreateUpdateToiletDto): Mono<ToiletDto>
    fun update(createUpdateToiletDto: CreateUpdateToiletDto): Mono<ToiletDto>
    fun addComment(createUpdateCommentDto: CreateUpdateCommentDto): Mono<Toilet>
    fun addComment(userId: ObjectId, createUpdateCommentDto: CreateUpdateCommentDto): Mono<Toilet>
    fun removeComment(commentId: String, toiletId: String): Mono<Toilet>
    fun getComments(toiletId: String): Flux<CommentDto>
    fun addRating(createUpdateRatingDto: CreateUpdateRatingDto): Mono<Toilet>
    fun addRating(userId: ObjectId, createUpdateRatingDto: CreateUpdateRatingDto): Mono<Toilet>
    fun removeRating(ratingId: String, value: Double, toiletId: String): Mono<Toilet>
    fun getNearbyToilets(lon: Double, lat: Double, maxDistanceInMeters: Double): Flux<ToiletDto>
    fun getById(id: String): Mono<Toilet>
    fun getAll(): Flux<ToiletDto>
    fun delete(id: String): Mono<Void>
    fun createAndLinkImage(file: Mono<FilePart>, toiletId: String): Mono<ToiletDto>
    fun linkImage(imageId: String, toiletId: String): Mono<ToiletDto>
}

@Service
class DefaultToiletService(
    @Autowired private val toiletRepository: ToiletRepository,
    @Autowired private val commentService: CommentService,
    @Autowired private val ratingService: RatingService,
    @Autowired private val imageService: ImageService,
    @Autowired private val userService: UserService
) : ToiletService {

    /**
     * Returns a [ToiletDto] instance out of the given [toilet] and [distance].
     * The [Toilet.previewID] is converted to a download URL of the image.
     * Comments and images need to be fetched separately, if needed.
     */
    fun createToiletDto(toilet: Toilet, distance: Double = 0.0) = ToiletDto(
        toilet.id.toHexString(),
        toilet.title,
        toilet.location,
        distance,
        getPreviewURL(toilet),
        toilet.averageRating,
        toilet.disabled,
        toilet.toiletCrewApproved,
        toilet.description,
        mutableListOf(),
        mutableListOf()
    )

    private fun getPreviewURL(toilet: Toilet): String {
        return when {
            toilet.previewID != null -> "/previews/${toilet.previewID.toHexString()}"
            else -> ""
        }
    }

    override fun create(createUpdateToiletDto: CreateUpdateToiletDto): Mono<ToiletDto> {
        LOG.debug("create: $createUpdateToiletDto")
        return toiletRepository
            .save(
                Toilet(
                    title = createUpdateToiletDto.title,
                    location = createUpdateToiletDto.location,
                    disabled = createUpdateToiletDto.disabled,
                    toiletCrewApproved = createUpdateToiletDto.toiletCrewApproved,
                    description = createUpdateToiletDto.description
                )
            )
            .map { createToiletDto(it) }
    }

    override fun update(createUpdateToiletDto: CreateUpdateToiletDto): Mono<ToiletDto> {
        LOG.debug("update: $createUpdateToiletDto")
        return getById(createUpdateToiletDto.id)
            .flatMap {
                toiletRepository.save(
                    it.copy(
                        title = createUpdateToiletDto.title,
                        location = createUpdateToiletDto.location,
                        disabled = createUpdateToiletDto.disabled,
                        toiletCrewApproved = createUpdateToiletDto.toiletCrewApproved,
                        description = createUpdateToiletDto.description
                    )
                )
            }
            .map { createToiletDto(it) }
    }

    override fun addComment(createUpdateCommentDto: CreateUpdateCommentDto) =
        addComment(userService.getCurrentUserId(), createUpdateCommentDto)

    override fun addComment(userId: ObjectId, createUpdateCommentDto: CreateUpdateCommentDto): Mono<Toilet> {
        LOG.debug("addComment: (userId=$userId, createUpdateCommentDto=$createUpdateCommentDto)")

        return commentService
            .create(userId, createUpdateCommentDto.text)
            .zipWith(getById(createUpdateCommentDto.toiletId))
            .flatMap {
                val comment = it.t1
                val toilet = it.t2

                LOG.debug("Adding comment '${comment.text}' to toilet with ${toilet.commentRefs.size} comments")

                toiletRepository.save(toilet.apply { commentRefs.add(ObjectId(comment.id)) })
            }
    }

    override fun removeComment(commentId: String, toiletId: String): Mono<Toilet> {
        LOG.debug("removeComment: (commentId=$commentId, toiletId=$toiletId)")
        return commentService
            .delete(commentId)
            .flatMap { getById(toiletId) }
            .flatMap { toilet ->
                toiletRepository.save(toilet.apply { commentRefs.remove(ObjectId(commentId)) })
            }
    }

    override fun getComments(toiletId: String): Flux<CommentDto> {
        LOG.debug("getComments: (toiletId=$toiletId)")
        return getById(toiletId)
            // retrieve comments by IDsh
            .map { it.commentRefs }
            .flatMapMany { Flux.fromIterable(it) }
            .flatMap { commentService.getById(it.toHexString()) }
            // and add user name information to each comment
            .flatMap { Mono.just(it).zipWith(userService.getById(it.userRef.toHexString())) }
            .map { commentService.createCommentDto(it.t1, it.t2) }
            .sort { o1, o2 -> o2.date.compareTo(o1.date) }
    }


    override fun addRating(createUpdateRatingDto: CreateUpdateRatingDto) =
        addRating(userService.getCurrentUserId(), createUpdateRatingDto)

    override fun addRating(userId: ObjectId, createUpdateRatingDto: CreateUpdateRatingDto): Mono<Toilet> {
        LOG.debug("addRating: (userId=$userId, createUpdateRatingDto=$createUpdateRatingDto)")

        return ratingService
            .create(userId, createUpdateRatingDto.value)
            .zipWith(getById(createUpdateRatingDto.toiletId))
            .flatMap {
                val rating = it.t1
                val toilet = it.t2

                LOG.debug("Adding rating '${rating.value}' to toilet with ${toilet.ratingRefs.size} ratings")

                toiletRepository.save(toilet.apply {
                    ratingRefs.add(ObjectId(rating.id))

                    // sum of all values
                    averageRating *= (ratingRefs.size - 1)
                    // add new rating value
                    averageRating += rating.value
                    // calculate average
                    averageRating /= ratingRefs.size
                })
            }
    }

    override fun removeRating(ratingId: String, value: Double, toiletId: String): Mono<Toilet> {
        LOG.debug("removeRating: (ratingId=$ratingId, value=$value, toiletId=$toiletId)")
        return ratingService
            .delete(ratingId)
            .flatMap {
                getById(toiletId)
            }
            .flatMap { toilet ->
                toiletRepository.save(toilet.apply {
                    ratingRefs.remove(ObjectId(ratingId))

                    if (ratingRefs.isEmpty()) {
                        averageRating = 0.0
                    } else {
                        // sum of all values
                        averageRating *= (ratingRefs.size + 1)
                        // remove rating value
                        averageRating -= value
                        // calculate average
                        averageRating /= ratingRefs.size
                    }
                })
            }
    }

    override fun getNearbyToilets(lon: Double, lat: Double, maxDistanceInMeters: Double): Flux<ToiletDto> {
        LOG.debug("getNearbyToilets: (lon=$lon, lat=$lat, maxDistanceInMeters=$maxDistanceInMeters)")
        val position = Point(lon, lat)
        val maxDistance = Distance(maxDistanceInMeters / 1000, Metrics.KILOMETERS)
        return toiletRepository
            .findByLocationNear(position, maxDistance)
            .map { createToiletDto(it.content, distanceToMeter(it.distance)) }
    }

    private fun distanceToMeter(distance: Distance): Double {
        return when (distance.metric) {
            Metrics.KILOMETERS -> distance.value * 1000
            Metrics.MILES -> distance.value * 1609.34
            else -> distance.value
        }
    }

    override fun getById(id: String): Mono<Toilet> {
        LOG.debug("getById: (id=$id)")
        return toiletRepository
            .findById(ObjectId(id))
            .switchIfEmpty(Mono.error(ToiletDoesNotExistException(id)))
    }

    override fun getAll(): Flux<ToiletDto> {
        LOG.debug("getAll")
        return toiletRepository
            .findAll()
            .map { createToiletDto(it) }
    }

    override fun delete(id: String): Mono<Void> {
        LOG.debug("delete: (id=$id)")
        return toiletRepository
            .findById(ObjectId(id))
            .flatMap { toilet ->
                Flux.merge(
                    // delete comments
                    Flux.fromIterable(toilet.commentRefs)
                        .flatMap { commentService.delete(it.toHexString()) },
                    // delete ratings
                    Flux.fromIterable(toilet.ratingRefs)
                        .flatMap { ratingService.delete(it.toHexString()) }
                ).then(
                    // finally -> delete toilet
                    toiletRepository.deleteById(toilet.id)
                )
            }
    }

    override fun createAndLinkImage(file: Mono<FilePart>, toiletId: String): Mono<ToiletDto> {
        LOG.debug("createAndLinkImage: (toiletId=$toiletId)")
        return getById(toiletId)
            .zipWith(imageService.create(file))
            .flatMap {
                val toilet = it.t1
                val fileId = it.t2
                toiletRepository.save(toilet.copy(previewID = ObjectId(fileId)))
            }
            .map { createToiletDto(it) }
    }

    override fun linkImage(imageId: String, toiletId: String): Mono<ToiletDto> {
        LOG.debug("linkImage: (imageId=$imageId, toiletId=$toiletId)")
        return getById(toiletId)
            .flatMap { toiletRepository.save(it.copy(previewID = ObjectId(imageId))) }
            .map { createToiletDto(it) }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultToiletService::class.java)
    }
}

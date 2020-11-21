package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.ToiletDoesNotExistException
import com.github.quillraven.toilapp.model.db.Toilet
import com.github.quillraven.toilapp.model.dto.CommentDto
import com.github.quillraven.toilapp.model.dto.CreateUpdateCommentDto
import com.github.quillraven.toilapp.model.dto.CreateUpdateRatingDto
import com.github.quillraven.toilapp.model.dto.CreateUpdateToiletDto
import com.github.quillraven.toilapp.model.dto.RatingDto
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
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuples

interface ToiletService {
    fun create(createUpdateToiletDto: CreateUpdateToiletDto): Mono<ToiletDto>
    fun getById(id: String): Mono<Toilet>
    fun update(createUpdateToiletDto: CreateUpdateToiletDto): Mono<ToiletDto>
    fun getAll(): Flux<ToiletDto>
    fun getNearbyToilets(lon: Double, lat: Double, maxDistanceInMeters: Double): Flux<ToiletDto>
    fun createComment(createUpdateCommentDto: CreateUpdateCommentDto): Mono<CommentDto>
    fun getComments(toiletId: String): Flux<CommentDto>
    fun createRating(createUpdateRatingDto: CreateUpdateRatingDto): Mono<RatingDto>
    fun updateRating(createUpdateRatingDto: CreateUpdateRatingDto): Mono<RatingDto>
    fun createAndLinkImage(file: Mono<FilePart>, toiletId: String): Mono<ToiletDto>
    fun delete(id: String): Mono<Void>
}

@Service
class DefaultToiletService(
    @Autowired private val toiletRepository: ToiletRepository,
    @Autowired private val imageService: ImageService,
    @Autowired private val commentService: CommentService,
    @Autowired private val ratingService: RatingService,
    @Autowired private val userService: UserService
) : ToiletService {
    /**
     * Returns a [ToiletDto] instance out of the given [toilet], [distance] and [averageRating].
     * The [Toilet.previewID] is converted to a download URL of the image.
     */
    fun createToiletDto(toilet: Toilet, distance: Double = 0.0, averageRating: Double = 0.0) = ToiletDto(
        toilet.id.toHexString(),
        toilet.title,
        toilet.description,
        toilet.location,
        distance,
        getPreviewURL(toilet),
        averageRating,
        toilet.disabled,
        toilet.toiletCrewApproved
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

    override fun getById(id: String): Mono<Toilet> {
        LOG.debug("getById: (id=$id)")
        return toiletRepository
            .findById(ObjectId(id))
            .switchIfEmpty(Mono.error(ToiletDoesNotExistException(id)))
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

    override fun getAll(): Flux<ToiletDto> {
        LOG.debug("getAll")
        return toiletRepository
            .findAll()
            .map { createToiletDto(it) }
    }

    private fun distanceToMeter(distance: Distance): Double {
        return when (distance.metric) {
            Metrics.KILOMETERS -> distance.value * 1000
            Metrics.MILES -> distance.value * 1609.34
            else -> distance.value
        }
    }

    override fun getNearbyToilets(lon: Double, lat: Double, maxDistanceInMeters: Double): Flux<ToiletDto> {
        LOG.debug("getNearbyToilets: (lon=$lon, lat=$lat, maxDistanceInMeters=$maxDistanceInMeters)")
        val position = Point(lon, lat)
        val maxDistance = Distance(maxDistanceInMeters / 1000, Metrics.KILOMETERS)
        return toiletRepository
            .findByLocationNear(position, maxDistance)
            .flatMap { geoResult ->
                Mono.just(geoResult).zipWith(toiletRepository.getRatingInfo(geoResult.content.id))
            }
            .map {
                val geoResult = it.t1
                val toilet = geoResult.content
                val toiletRatingInfo = it.t2

                LOG.debug("Rating info for toilet '${toilet.id}' is $toiletRatingInfo")

                createToiletDto(
                    toilet,
                    distanceToMeter(geoResult.distance),
                    toiletRatingInfo.averageRating
                )
            }
    }

    @Transactional
    override fun createComment(createUpdateCommentDto: CreateUpdateCommentDto): Mono<CommentDto> {
        val userId = userService.getCurrentUserId()
        LOG.debug("create: (userId=$userId, $createUpdateCommentDto")

        return getById(createUpdateCommentDto.toiletId)
            .flatMap { toilet ->
                commentService
                    .create(userId, createUpdateCommentDto.text)
                    .map { Tuples.of(toilet, it) }
            }
            .flatMap {
                val toilet = it.t1
                val comment = it.t2
                LOG.debug("Created comment $comment")
                toiletRepository.addComment(toilet.id, comment.id).map { comment }
            }
            .flatMap { comment ->
                userService.getById(userId).map { Tuples.of(comment, it) }
            }
            .map {
                commentService.createCommentDto(it.t1, it.t2)
            }
    }

    override fun getComments(toiletId: String): Flux<CommentDto> {
        LOG.debug("getComments: (toiletId=$toiletId)")
        return toiletRepository.getCommentInfo(ObjectId(toiletId))
            .flatMap {
                Flux.fromIterable(it.commentRefs)
            }
            .flatMap {
                commentService.getById(it.toHexString())
            }
            .flatMap { comment ->
                userService.getById(comment.userRef).map { Tuples.of(comment, it) }
            }
            .map {
                val comment = it.t1
                val user = it.t2
                LOG.debug("Creating CommentDto for comment $comment and user $user")
                commentService.createCommentDto(comment, user)
            }
    }

    @Transactional
    override fun createRating(createUpdateRatingDto: CreateUpdateRatingDto): Mono<RatingDto> {
        val userId = userService.getCurrentUserId()
        LOG.debug("createRating: (userId=$userId, $createUpdateRatingDto)")

        return getById(createUpdateRatingDto.toiletId)
            .flatMap { toilet ->
                ratingService.create(userId, createUpdateRatingDto.value).map {
                    Tuples.of(toilet, it)
                }
            }
            .flatMap {
                val toilet = it.t1
                val rating = it.t2

                LOG.debug("Created rating $rating")

                toiletRepository.addRating(toilet.id, rating.id, rating.value).map {
                    rating
                }
            }
            .flatMap { rating ->
                userService.getById(userId).map { Tuples.of(rating, it) }
            }
            .map {
                ratingService.createRatingDto(it.t1, it.t2)
            }
    }

    @Transactional
    override fun updateRating(createUpdateRatingDto: CreateUpdateRatingDto): Mono<RatingDto> {
        LOG.debug("updateRating: $createUpdateRatingDto")

        return ratingService.getById(createUpdateRatingDto.ratingId)
            .flatMap { rating ->
                ratingService.update(createUpdateRatingDto).map {
                    Tuples.of(rating.value, it)
                }
            }
            .flatMap {
                val rating = it.t2
                val oldValue = it.t1
                val newValue = rating.value

                LOG.debug("Update rating for toilet '${createUpdateRatingDto.toiletId}' from $oldValue to $newValue")

                toiletRepository.updateRating(ObjectId(createUpdateRatingDto.toiletId), oldValue, newValue).map {
                    rating
                }
            }
            .map {
                ratingService.createRatingDto(it)
            }
    }

    override fun delete(id: String): Mono<Void> {
        LOG.debug("delete: (id=$id)")
        return toiletRepository
            .findById(ObjectId(id))
//            .flatMap { toilet ->
//                Flux.merge(
//                    // delete comments
//                    Flux.fromIterable(toilet.commentRefs)
//                        .flatMap { commentService.delete(it.toHexString()) },
//                    // delete ratings
//                    Flux.fromIterable(toilet.ratingRefs)
//                        .flatMap { ratingService.delete(it.toHexString()) }
//                )
//            }
            .then(
                // finally -> delete toilet
                toiletRepository.deleteById(ObjectId(id))
            )
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

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultToiletService::class.java)
    }
}

package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.InvalidIdException
import com.github.quillraven.toilapp.ToiletDoesNotExistException
import com.github.quillraven.toilapp.model.db.Toilet
import com.github.quillraven.toilapp.model.dto.CreateUpdateToiletDto
import com.github.quillraven.toilapp.model.dto.GetNearbyToiletsDto
import com.github.quillraven.toilapp.model.dto.ToiletDetailsDto
import com.github.quillraven.toilapp.model.dto.ToiletOverviewDto
import com.github.quillraven.toilapp.repository.ToiletRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ToiletService {
    fun create(createUpdateToiletDto: CreateUpdateToiletDto): Mono<ToiletDetailsDto>
    fun update(createUpdateToiletDto: CreateUpdateToiletDto): Mono<ToiletDetailsDto>
    fun getNearbyToilets(getNearbyToiletsDto: GetNearbyToiletsDto): Flux<ToiletOverviewDto>
    fun getToiletDetails(id: String, lon: Double, lat: Double): Mono<ToiletDetailsDto>
    fun delete(id: String): Mono<Void>
}

@Service
class DefaultToiletService(
    @Autowired private val toiletRepository: ToiletRepository,
    @Autowired private val imageService: ImageService,
    @Autowired private val commentService: CommentService,
    @Autowired private val ratingService: RatingService
) : ToiletService {
    override fun create(createUpdateToiletDto: CreateUpdateToiletDto): Mono<ToiletDetailsDto> {
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
            // distance, URL, rating and num comments information is not necessary for a newly created toilet
            .map { it.createToiletDetailsDto(0.0, "", 0.0, 0) }
    }

    private fun getById(toiletId: String): Mono<Toilet> {
        LOG.debug("getById: (toiletId=$toiletId)")

        return when {
            !ObjectId.isValid(toiletId) -> Mono.error(InvalidIdException(toiletId))
            else -> toiletRepository
                .findById(ObjectId(toiletId))
                .switchIfEmpty(Mono.error(ToiletDoesNotExistException(toiletId)))
        }
    }

    override fun update(createUpdateToiletDto: CreateUpdateToiletDto): Mono<ToiletDetailsDto> {
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
            // distance, URL, rating and num comments information is not necessary for an update call
            .map { it.createToiletDetailsDto(0.0, "", 0.0, 0) }
    }

    override fun getNearbyToilets(getNearbyToiletsDto: GetNearbyToiletsDto): Flux<ToiletOverviewDto> {
        LOG.debug("getNearbyToilets: (getNearbyToiletsDto=$getNearbyToiletsDto)")

        return toiletRepository.getNearbyToilets(
            getNearbyToiletsDto.location,
            getNearbyToiletsDto.radiusInKm,
            getNearbyToiletsDto.maxToiletsToLoad,
            getNearbyToiletsDto.minDistanceInKm,
            getNearbyToiletsDto.toiletIdsToExclude
        )
            .flatMapSequential { toiletDistanceInfo ->
                Mono.zip(
                    Mono.just(toiletDistanceInfo),
                    ratingService.getAverageRating(toiletDistanceInfo.id),
                    imageService.getPreviewURL(toiletDistanceInfo.id)
                )
            }
            .map {
                val toiletDistanceInfo = it.t1
                val averageRating = it.t2
                val previewUrl = it.t3

                toiletDistanceInfo.createToiletOverviewDto(
                    previewUrl,
                    averageRating
                )
            }
    }

    private fun fetchToiletDetails(toilet: Toilet, lon: Double, lat: Double) =
        Mono.zip(
            Mono.just(toilet),
            imageService.getPreviewURL(toilet.id),
            ratingService.getAverageRating(toilet.id),
            commentService.getNumComments(toilet.id),
            toiletRepository.getDistanceBetween(toilet.id, GeoJsonPoint(lon, lat))
        )

    override fun getToiletDetails(id: String, lon: Double, lat: Double): Mono<ToiletDetailsDto> {
        LOG.debug("getToiletDetails: (id=$id, lon=$lon, lat=$lat)")

        return getById(id)
            .flatMap { fetchToiletDetails(it, lon, lat) }
            .map {
                val toilet = it.t1
                val previewUrl = it.t2
                val rating = it.t3
                val numComments = it.t4
                val distanceInfo = it.t5

                toilet.createToiletDetailsDto(distanceInfo.distance, previewUrl, rating, numComments)
            }
    }

    @Transactional
    override fun delete(id: String): Mono<Void> {
        LOG.debug("delete: (id=$id)")

        return when {
            !ObjectId.isValid(id) -> Mono.error(InvalidIdException(id))
            else -> {
                val toiletId = ObjectId(id)

                Flux
                    .merge(
                        commentService.deleteByToiletId(toiletId),
                        ratingService.deleteByToiletId(toiletId),
                        imageService.deleteByToiletId(toiletId)
                    )
                    .then(
                        toiletRepository.deleteById(toiletId)
                    )
            }
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultToiletService::class.java)
    }
}

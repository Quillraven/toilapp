package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.InvalidIdException
import com.github.quillraven.toilapp.ToiletDoesNotExistException
import com.github.quillraven.toilapp.model.db.Toilet
import com.github.quillraven.toilapp.model.dto.CreateUpdateToiletDto
import com.github.quillraven.toilapp.model.dto.ToiletDetailsDto
import com.github.quillraven.toilapp.model.dto.ToiletOverviewDto
import com.github.quillraven.toilapp.repository.ToiletRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Metrics
import org.springframework.data.geo.Point
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ToiletService {
    fun create(createUpdateToiletDto: CreateUpdateToiletDto): Mono<ToiletDetailsDto>
    fun update(createUpdateToiletDto: CreateUpdateToiletDto): Mono<ToiletDetailsDto>
    fun getNearbyToilets(lon: Double, lat: Double, maxDistanceInMeters: Double): Flux<ToiletOverviewDto>
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
                    location = GeoJsonPoint(createUpdateToiletDto.location),
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
                        location = GeoJsonPoint(createUpdateToiletDto.location),
                        disabled = createUpdateToiletDto.disabled,
                        toiletCrewApproved = createUpdateToiletDto.toiletCrewApproved,
                        description = createUpdateToiletDto.description
                    )
                )
            }
            // distance, URL, rating and num comments information is not necessary for an update call
            .map { it.createToiletDetailsDto(0.0, "", 0.0, 0) }
    }

    private fun distanceToMeter(distance: Distance): Double {
        return when (distance.metric) {
            Metrics.KILOMETERS -> distance.value * 1000
            Metrics.MILES -> distance.value * 1609.34
            else -> distance.value
        }
    }

    override fun getNearbyToilets(lon: Double, lat: Double, maxDistanceInMeters: Double): Flux<ToiletOverviewDto> {
        LOG.debug("getNearbyToilets: (lon=$lon, lat=$lat, maxDistanceInMeters=$maxDistanceInMeters)")

        return toiletRepository
            .findByLocationNear(Point(lon, lat), Distance(maxDistanceInMeters / 1000, Metrics.KILOMETERS))
            .flatMapSequential { geoResult ->
                val toilet = geoResult.content

                Mono.zip(
                    Mono.just(geoResult),
                    ratingService.getAverageRating(toilet),
                    imageService.getPreviewURL(toilet.id)
                )
            }
            .map {
                val geoResult = it.t1
                val toilet = geoResult.content
                val averageRating = it.t2
                val previewUrl = it.t3

                toilet.createToiletOverviewDto(
                    distanceToMeter(geoResult.distance),
                    previewUrl,
                    averageRating
                )
            }
    }

    private fun fetchToiletDetails(toilet: Toilet, lon: Double, lat: Double) =
        Mono.zip(
            Mono.just(toilet),
            imageService.getPreviewURL(toilet.id),
            ratingService.getAverageRating(toilet),
            commentService.getNumComments(toilet.id),
            toiletRepository.getDistanceBetween(toilet.id, Point(lon, lat))
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

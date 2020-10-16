package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.ToiletDoesNotExistException
import com.github.quillraven.toilapp.model.db.Toilet
import com.github.quillraven.toilapp.model.dto.ToiletDto
import com.github.quillraven.toilapp.repository.ToiletRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Metrics
import org.springframework.data.geo.Point
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ToiletService {
    fun create(toilet: Toilet): Mono<ToiletDto>
    fun update(id: String, toilet: Toilet): Mono<ToiletDto>
    fun addComment(commentId: String, toilet: Toilet): Mono<Toilet>
    fun getNearbyToilets(lon: Double, lat: Double, maxDistanceInMeters: Double): Flux<ToiletDto>
    fun getById(id: String): Mono<Toilet>
    fun getAll(): Flux<ToiletDto>
    fun delete(id: String): Mono<Void>
}

@Service
class DefaultToiletService(
    @Autowired private val toiletRepository: ToiletRepository
) : ToiletService {
    /**
     * Returns a [ToiletDto] instance out of the given [toilet] and [distance].
     * Comments and images need to be fetched separately, if needed.
     */
    fun createToiletDto(toilet: Toilet, distance: Double = 0.0) = ToiletDto(
        toilet.id.toHexString(),
        toilet.title,
        toilet.location,
        distance,
        if (toilet.previewID != null) toilet.previewID.toHexString() else "",
        toilet.rating,
        toilet.disabled,
        toilet.toiletCrewApproved,
        toilet.description,
        mutableListOf(),
        mutableListOf()
    )

    override fun create(toilet: Toilet): Mono<ToiletDto> {
        LOG.debug("create: $toilet")
        return toiletRepository
            .save(toilet)
            .map { createToiletDto(it) }
    }

    override fun update(id: String, toilet: Toilet): Mono<ToiletDto> {
        LOG.debug("update: (id=$id, toilet=$toilet)")
        return getById(id)
            .flatMap { toiletRepository.save(toilet.copy(id = ObjectId(id))) }
            .map { createToiletDto(it) }
    }

    override fun addComment(commentId: String, toilet: Toilet): Mono<Toilet> {
        LOG.debug("addComment: (commentId=$commentId, toilet=$toilet)")
        return toiletRepository.save(toilet.apply { commentRefs.add(ObjectId(commentId)) })
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
        return getById(id)
            .flatMap { toiletRepository.deleteById(ObjectId(id)) }
        // TODO should also delete all comments and images
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultToiletService::class.java)
    }
}

package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.ToiletDoesNotExistException
import com.github.quillraven.toilapp.dto.ToiletResultDto
import com.github.quillraven.toilapp.model.Toilet
import com.github.quillraven.toilapp.repository.ToiletRepository
import org.slf4j.LoggerFactory
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Metrics
import org.springframework.data.geo.Point
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ToiletService {
    fun create(toilet: Toilet): Mono<Toilet>
    fun update(id: String, toilet: Toilet): Mono<Toilet>
    fun getNearbyToilets(x: Double, y: Double, maxDistanceInMeters: Double): Flux<ToiletResultDto>
    fun getById(id: String): Mono<Toilet>
    fun getAll(): Flux<Toilet>
    fun delete(id: String): Mono<Void>
}

@Service
class DefaultToiletService(private val toiletRepository: ToiletRepository) : ToiletService {
    private val LOG = LoggerFactory.getLogger(ToiletService::class.java)

    override fun create(toilet: Toilet): Mono<Toilet> {
        LOG.debug("create: $toilet")
        return toiletRepository.save(toilet)
    }

    override fun update(id: String, toilet: Toilet): Mono<Toilet> {
        LOG.debug("update: (id=$id, toilet=$toilet)")
        return getById(id)
            .flatMap { toiletRepository.save(toilet.copy(id = id)) }
    }

    override fun getNearbyToilets(x: Double, y: Double, maxDistanceInMeters: Double): Flux<ToiletResultDto> {
        LOG.debug("getNearbyToilets: (x=$x, y=$y, maxDistanceInMeters=$maxDistanceInMeters)")
        val position = Point(x, y)
        val maxDistance = Distance(maxDistanceInMeters / 1000, Metrics.KILOMETERS)
        //return toiletRepository.getNearbyToilets(x, y, maxDistanceInMeters)
        val res = toiletRepository.findByLocationNear(position, maxDistance)
        return res.map { r -> ToiletResultDto(r.content, distanceToMeter(r.distance))}
    }

    private fun distanceToMeter(distance: Distance): Double {
        if(distance.metric == Metrics.KILOMETERS) {
            return distance.value * 1000
        } else if(distance.metric == Metrics.MILES) {
            return distance.value * 1609.34
        } else {
            return distance.value * 1000
        }
    }

    override fun getById(id: String): Mono<Toilet> {
        LOG.debug("getById: (id=$id)")
        return toiletRepository
            .findById(id)
            .switchIfEmpty(Mono.error(ToiletDoesNotExistException(id)))
    }

    override fun getAll(): Flux<Toilet> {
        LOG.debug("getAll")
        return toiletRepository.findAll()
    }

    override fun delete(id: String): Mono<Void> {
        LOG.debug("delete: (id=$id)")
        return toiletRepository.findById(id)
            .switchIfEmpty(Mono.error(ToiletDoesNotExistException(id)))
            .flatMap { toiletRepository.deleteById(id) }
    }
}

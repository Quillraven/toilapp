package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.ToiletDoesNotExistException
import com.github.quillraven.toilapp.model.Toilet
import com.github.quillraven.toilapp.repository.ToiletRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ToiletService {
    fun create(toilet: Toilet): Mono<Toilet>
    fun update(id: String, toilet: Toilet): Mono<Toilet>
    fun getNearbyToilets(x: Double, y: Double, maxDistanceInMeters: Double): Flux<Toilet>
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

    override fun getNearbyToilets(x: Double, y: Double, maxDistanceInMeters: Double): Flux<Toilet> {
        LOG.debug("getNearbyToilets: (x=$x, y=$y, maxDistanceInMeters=$maxDistanceInMeters)")
        return toiletRepository.getNearbyToilets(x, y, maxDistanceInMeters)
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

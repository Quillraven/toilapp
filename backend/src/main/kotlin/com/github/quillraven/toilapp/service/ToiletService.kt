package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.ToiletDoesNotExistException
import com.github.quillraven.toilapp.model.Toilet
import com.github.quillraven.toilapp.repository.ToiletRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface IToiletService {
    fun create(toilet: Toilet): Mono<Toilet>
    fun update(id: String, toilet: Toilet): Mono<Toilet>
    fun getNearbyToilets(x: Double, y: Double, maxDistanceInMeters: Double): Flux<Toilet>
    fun getAll(): Flux<Toilet>
    fun delete(id: String): Mono<Void>
}

private val LOG = LoggerFactory.getLogger(ToiletService::class.java)

@Service
class ToiletService(private val toiletRepository: ToiletRepository) : IToiletService {
    override fun create(toilet: Toilet): Mono<Toilet> {
        LOG.debug("create: $toilet")
        return toiletRepository.save(toilet)
    }

    override fun update(id: String, toilet: Toilet): Mono<Toilet> {
        LOG.debug("update: (id=$id, toilet=$toilet)")
        return toiletRepository.findById(id)
            .flatMap { toiletRepository.save(toilet.copy(id = id)) }
            .switchIfEmpty(Mono.error(ToiletDoesNotExistException(id)))
    }

    override fun getNearbyToilets(x: Double, y: Double, maxDistanceInMeters: Double): Flux<Toilet> {
        LOG.debug("getNearbyToilets: (x=$x, y=$y, maxDistanceInMeters=$maxDistanceInMeters)")
        return toiletRepository.getNearbyToilets(x, y, maxDistanceInMeters)
    }

    override fun getAll(): Flux<Toilet> {
        LOG.debug("getAll")
        return toiletRepository.findAll()
    }

    override fun delete(id: String): Mono<Void> {
        LOG.debug("delete: (id=$id)")
        return toiletRepository.deleteById(id)
    }
}
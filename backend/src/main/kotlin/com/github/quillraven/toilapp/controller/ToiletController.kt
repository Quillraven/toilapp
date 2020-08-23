package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.ToiletDoesNotExistException
import com.github.quillraven.toilapp.model.Toilet
import com.github.quillraven.toilapp.repository.ToiletRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

private val LOG = LoggerFactory.getLogger(ToiletController::class.java)

fun <T : Any> KClass<out T>.getNonNullProperties(vararg exceptions: KProperty1<T, *>) =
    declaredMemberProperties
        .filter { !it.returnType.isMarkedNullable && !exceptions.contains(it) }
        .map { it.name }
        .toTypedArray()

@RestController
@RequestMapping("/api")
class ToiletController {
    @Autowired
    lateinit var toiletRepository: ToiletRepository

    @PostMapping("/toilets")
    fun createToilet(@RequestBody toilet: Toilet): Mono<Toilet> {
        LOG.debug("createToilet: $toilet")
        return toiletRepository.save(toilet)
    }

    @PutMapping("/toilets/{id}")
    fun updateToilet(@PathVariable id: String, @RequestBody toilet: Toilet): Mono<Toilet> {
        LOG.debug("updateToilet: (id=$id, toilet=$toilet)")
        return toiletRepository.findById(id)
            .flatMap { toiletRepository.save(toilet.copy(id = id)) }
            .switchIfEmpty(Mono.error(ToiletDoesNotExistException(id)))
    }

    @GetMapping("/toilets")
    fun getNearbyToilets(
        @RequestParam(required = false) x: Double?,
        @RequestParam(required = false) y: Double?,
        @RequestParam(required = false) maxDistanceInMeters: Double?
    ): Flux<Toilet> {
        LOG.debug("getNearbyToilets: (x=$x, y=$y, maxDistanceInMeters=$maxDistanceInMeters)")
        return when {
            x == null || y == null || maxDistanceInMeters == null -> {
                toiletRepository.findAll()
            }
            else -> {
                toiletRepository.getNearbyToilets(x, y, maxDistanceInMeters)
            }
        }
    }

    @DeleteMapping("/toilets/{id}")
    fun deleteToilet(@PathVariable id: String): Mono<Void> {
        LOG.debug("deleteToilet: (id=$id)")
        return toiletRepository.deleteById(id)
    }
}

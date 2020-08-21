package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.model.Toilet
import com.github.quillraven.toilapp.repository.ToiletRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.contains
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

private val LOG = LoggerFactory.getLogger(ToiletController::class.java)

fun <T : Any> KClass<out T>.getNonNullProperties(vararg exceptions: KProperty1<T, String>) =
    declaredMemberProperties
        .filter { !it.returnType.isMarkedNullable && !exceptions.contains(it) }
        .map { it.name }
        .toTypedArray()

@RestController
@RequestMapping("/api")
class ToiletController {
    @Autowired
    lateinit var toiletRepository: ToiletRepository

    @GetMapping("/toilets/{location}")
    fun getNearbyToilets(@PathVariable("location") location: String): Flux<Toilet> {
        LOG.debug("getNearbyToiler: $location")
        return toiletRepository.findAll(
            Example.of(
                Toilet(location = location),
                ExampleMatcher.matching()
                    .withMatcher(Toilet::location.name, contains())
                    .withIgnorePaths(*Toilet::class.getNonNullProperties(Toilet::location))
            ),
            Sort.by(Toilet::location.name)
        )
    }

    @GetMapping("/toilets")
    fun getAllToilets(): Flux<Toilet> {
        LOG.debug("getAllToilets")
        return toiletRepository.findAll()
    }

    @PostMapping("/toilets")
    fun createToilet(@RequestBody toilet: Toilet): Mono<Toilet> {
        LOG.debug("createToilet: $toilet")
        return toiletRepository.save(toilet)
    }
}

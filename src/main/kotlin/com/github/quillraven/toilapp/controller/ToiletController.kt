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
import reactor.kotlin.core.publisher.toMono

private val LOG = LoggerFactory.getLogger(ToiletController::class.java)

@RestController
@RequestMapping("/api")
class ToiletController {

    @Autowired
    lateinit var toiletRepository: ToiletRepository

    @GetMapping("/toilets/{location}")
    fun getNearbyToilets(@PathVariable("location") location: String): Flux<Toilet> {
        return toiletRepository.findAll(
            Example.of(
                Toilet(location = location),
                ExampleMatcher.matchingAny().withMatcher("location", contains())
            ),
            Sort.by("location")
        ).apply {
            subscribe { toilet ->
                LOG.debug("Found nearby toilet: $toilet")
            }
        }
    }

    @GetMapping("/toilets")
    fun getAllToilets(): Flux<Toilet> {
        return toiletRepository.findAll().apply {
            subscribe { toilet ->
                LOG.debug("Found toilet: $toilet")
            }
        }
    }

    @PostMapping("/toilets")
    fun createToilet(@RequestBody toilet: Toilet): Mono<Toilet> {
        return toiletRepository.save(toilet).toMono().apply {
            subscribe { newToilet ->
                LOG.debug("Created new toilet: $newToilet")
            }
        }
    }
}

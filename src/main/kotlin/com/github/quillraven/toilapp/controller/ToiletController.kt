package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.model.Toilet
import com.github.quillraven.toilapp.repository.ToiletRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Example
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api")
class ToiletController {
    @Autowired
    lateinit var toiletRepository: ToiletRepository

    @GetMapping("/toilets/{location}")
    fun getNearbyToilets(@PathVariable("location") location: String): Flux<Toilet> =
        toiletRepository.findAll(
            Example.of(Toilet(location = location)),
            Sort.by("location")
        )

    @PostMapping("/toilets")
    fun createToilet(@RequestBody toilet: Toilet): Mono<Toilet> =
        toiletRepository.save(toilet)
}

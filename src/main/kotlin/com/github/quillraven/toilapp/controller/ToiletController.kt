package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.model.Toilet
import com.github.quillraven.toilapp.repository.ToiletRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api")
class ToiletController {
    @Autowired
    lateinit var toiletRepository: ToiletRepository

    @GetMapping("/toilets/{location}")
    fun getNearbyToilets(@PathVariable("location") location: String): ResponseEntity<Array<Toilet>> {
        try {
            val nearbyToilets = toiletRepository.findByLocation(location)
            if (nearbyToilets.isEmpty()) {
                return ResponseEntity(HttpStatus.NO_CONTENT)
            }
            return ResponseEntity(nearbyToilets, HttpStatus.OK)
        } catch (e: Exception) {
            return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/toilets")
    fun createToilet(@RequestBody toilet: Toilet): ResponseEntity<Toilet> {
        try {
            val newToilet = toiletRepository.save(toilet.copy())
            return ResponseEntity(newToilet, HttpStatus.CREATED)
        } catch (e: Exception) {
            return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}

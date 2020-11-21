package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.model.dto.CreateUpdateRatingDto
import com.github.quillraven.toilapp.service.RatingService
import com.github.quillraven.toilapp.service.ToiletService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class RatingController(
    @Autowired private val ratingService: RatingService,
    @Autowired private val toiletService: ToiletService
) {
    @PostMapping("/ratings")
    fun createRating(@RequestBody createUpdateRatingDto: CreateUpdateRatingDto) =
        toiletService.createRating(createUpdateRatingDto)

    @PutMapping("/ratings")
    fun updateRating(@RequestBody createUpdateRatingDto: CreateUpdateRatingDto) =
        toiletService.updateRating(createUpdateRatingDto)

    @DeleteMapping("/ratings/{id}")
    fun deleteRating(@PathVariable id: String) = ratingService.delete(id)
}

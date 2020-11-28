package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.model.dto.CreateUpdateRatingDto
import com.github.quillraven.toilapp.service.RatingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/ratings")
class RatingController(
    @Autowired private val ratingService: RatingService
) {
    @PostMapping
    fun create(@RequestBody createUpdateRatingDto: CreateUpdateRatingDto) =
        ratingService.create(createUpdateRatingDto)

    @PutMapping
    fun update(@RequestBody createUpdateRatingDto: CreateUpdateRatingDto) =
        ratingService.update(createUpdateRatingDto)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) = ratingService.delete(id)
}

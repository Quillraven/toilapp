package com.github.quillraven.toilapp.model.dto

data class CreateUpdateRatingDto(
    val ratingId: String = "",
    val toiletId: String = "",
    val value: Double = 0.0
)

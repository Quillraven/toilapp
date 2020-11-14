package com.github.quillraven.toilapp.model.dto

data class RatingDto(
    val id: String,
    val user: UserDto,
    val value: Double
)

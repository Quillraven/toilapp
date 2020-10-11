package com.github.quillraven.toilapp.dto

import com.github.quillraven.toilapp.model.Toilet

data class ToiletResultDto(
        val toilet: Toilet,
        val distance: Double
)
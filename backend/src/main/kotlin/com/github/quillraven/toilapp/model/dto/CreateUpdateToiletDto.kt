package com.github.quillraven.toilapp.model.dto

import org.springframework.data.geo.Point

data class CreateUpdateToiletDto(
    val id: String = "",
    val title: String,
    val description: String,
    val location: Point,
    val disabled: Boolean = false,
    val toiletCrewApproved: Boolean = false
)

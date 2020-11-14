package com.github.quillraven.toilapp.model.dto

import org.springframework.data.mongodb.core.geo.GeoJsonPoint

data class CreateUpdateToiletDto(
    val id: String = "",
    val title: String,
    val description: String,
    val location: GeoJsonPoint,
    val disabled: Boolean = false,
    val toiletCrewApproved: Boolean = false
)

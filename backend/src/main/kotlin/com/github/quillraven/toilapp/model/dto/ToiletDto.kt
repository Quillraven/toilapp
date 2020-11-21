package com.github.quillraven.toilapp.model.dto

import org.springframework.data.mongodb.core.geo.GeoJsonPoint

data class ToiletDto(
    val id: String,
    val title: String,
    val description: String,
    val location: GeoJsonPoint,
    val distance: Double,
    val previewURL: String,
    val rating: Double,
    val disabled: Boolean,
    val toiletCrewApproved: Boolean
)

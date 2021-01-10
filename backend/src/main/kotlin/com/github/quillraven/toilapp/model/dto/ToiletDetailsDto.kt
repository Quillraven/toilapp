package com.github.quillraven.toilapp.model.dto

import org.springframework.data.geo.Point

data class ToiletDetailsDto(
    val id: String,
    val title: String,
    val description: String,
    val location: Point,
    val distance: Double,
    val previewURL: String,
    val rating: Double,
    val numComments: Long,
    val disabled: Boolean,
    val toiletCrewApproved: Boolean
)

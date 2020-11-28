package com.github.quillraven.toilapp.model.dto

data class ToiletOverviewDto(
    val id: String,
    val title: String,
    val distance: Double,
    val previewURL: String,
    val rating: Double,
    val disabled: Boolean,
    val toiletCrewApproved: Boolean
)

package com.github.quillraven.toilapp.model.dto

import org.springframework.data.mongodb.core.geo.GeoJsonPoint

data class ToiletDto(
    val id: String,
    val title: String,
    val location: GeoJsonPoint,
    val distance: Double,
    val previewURL: String,
    val rating: Double,
    val disabled: Boolean,
    val toiletCrewApproved: Boolean,
    val description: String,
    val comments: MutableList<CommentDto>,
    val images: MutableList<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ToiletDto

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

package com.github.quillraven.toilapp.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "toilets")
data class Toilet(
    @Id
    val id: String = "",
    val title: String = "",
    val location: GeoJsonPoint = GeoJsonPoint(0.0, 0.0),
    val preview: String = "",
    val rating: Int = 0,
    val disabled: Boolean = false,
    val toiletCrewApproved: Boolean = false,
    val description: String = "",
    val comments: Array<Comment>? = null,
    val images: Array<String>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Toilet

        if (title != other.title) return false
        if (location != other.location) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + location.hashCode()
        return result
    }
}

package com.github.quillraven.toilapp.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "toilets")
data class Toilet(
    @Id
    val id: ObjectId = ObjectId.get(),
    val title: String = "",
    @GeoSpatialIndexed(name = "location", type = GeoSpatialIndexType.GEO_2DSPHERE)
    val location: GeoJsonPoint = GeoJsonPoint(0.0, 0.0),
    val previewID: String = "",
    val rating: Double = 0.0, //mean rating -> Double
    val disabled: Boolean = false,
    val toiletCrewApproved: Boolean = false,
    val description: String = "",
    val comments: Array<Comment>? = arrayOf(),
    val images: Array<String>? = arrayOf()
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

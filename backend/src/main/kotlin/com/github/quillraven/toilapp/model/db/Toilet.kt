package com.github.quillraven.toilapp.model.db

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "toilets")
data class Toilet(
    @Id
    val id: ObjectId = ObjectId(),
    val title: String = "",
    @GeoSpatialIndexed(name = "location", type = GeoSpatialIndexType.GEO_2DSPHERE)
    val location: GeoJsonPoint = GeoJsonPoint(0.0, 0.0),
    val previewID: ObjectId? = null,
    var averageRating: Double = 0.0,
    val ratingRefs: MutableList<ObjectId> = mutableListOf(),
    val disabled: Boolean = false,
    val toiletCrewApproved: Boolean = false,
    val description: String = "",
    val commentRefs: MutableList<ObjectId> = mutableListOf(),
    val imageRefs: MutableList<ObjectId> = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Toilet

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

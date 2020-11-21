package com.github.quillraven.toilapp.model.db

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed
import org.springframework.data.mongodb.core.mapping.Document

const val TOILETS_COLLECTION_NAME = "toilets"

@Document(collection = TOILETS_COLLECTION_NAME)
data class Toilet(
    @Id
    val id: ObjectId = ObjectId(),
    val title: String = "",
    val description: String = "",
    @GeoSpatialIndexed(name = "location", type = GeoSpatialIndexType.GEO_2DSPHERE)
    val location: GeoJsonPoint = GeoJsonPoint(0.0, 0.0),
    val previewID: ObjectId? = null,
    val totalRating: Int = 0,
    val disabled: Boolean = false,
    val toiletCrewApproved: Boolean = false
)

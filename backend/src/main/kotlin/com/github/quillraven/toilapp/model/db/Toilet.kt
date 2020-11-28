package com.github.quillraven.toilapp.model.db

import com.github.quillraven.toilapp.model.dto.ToiletDetailsDto
import com.github.quillraven.toilapp.model.dto.ToiletOverviewDto
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
    val disabled: Boolean = false,
    val toiletCrewApproved: Boolean = false
) {
    fun createToiletDetailsDto(
        distance: Double,
        previewURL: String,
        rating: Double,
        numComments: Long
    ) = ToiletDetailsDto(
        id = id.toHexString(),
        title = title,
        description = description,
        location = location,
        distance = distance,
        previewURL = previewURL,
        rating = rating,
        numComments = numComments,
        disabled = disabled,
        toiletCrewApproved = toiletCrewApproved
    )

    fun createToiletOverviewDto(
        distance: Double,
        previewURL: String,
        rating: Double
    ) = ToiletOverviewDto(
        id = id.toHexString(),
        title = title,
        distance = distance,
        previewURL = previewURL,
        rating = rating,
        disabled = disabled,
        toiletCrewApproved = toiletCrewApproved
    )
}

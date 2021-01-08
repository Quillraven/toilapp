package com.github.quillraven.toilapp.model.dto

import org.springframework.data.mongodb.core.geo.GeoJsonPoint

data class GetNearbyToiletsDto(
    val location: GeoJsonPoint,
    val radiusInKm: Double,
    val maxToiletsToLoad: Long,
    val minDistanceInKm: Double = 0.0,
    val toiletIdsToExclude: Set<String> = setOf()
)

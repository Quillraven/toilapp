package com.github.quillraven.toilapp.model.db

import com.github.quillraven.toilapp.model.dto.ToiletOverviewDto
import org.bson.types.ObjectId

data class ToiletDistanceInfo(
    val id: ObjectId = ObjectId(),
    val title: String = "",
    val disabled: Boolean = false,
    val toiletCrewApproved: Boolean = false,
    val distance: Double = 0.0
) {
    fun createToiletOverviewDto(
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

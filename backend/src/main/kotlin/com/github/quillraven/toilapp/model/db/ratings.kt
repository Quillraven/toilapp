package com.github.quillraven.toilapp.model.db

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "ratings")
data class Rating(
    @Id
    val id: ObjectId = ObjectId(),
    val userRef: ObjectId = ObjectId(),
    val value: Int = 0
)

data class ToiletRatingInfo(
    @Id
    val id: ObjectId = ObjectId(),
    val totalRating: Int = 0,
    val numRatings: Int = 0
) {
    val averageRating: Double
        get() = when {
            numRatings <= 0 -> 0.0
            else -> totalRating.toDouble() / numRatings
        }
}

package com.github.quillraven.toilapp.model.db

import com.github.quillraven.toilapp.model.dto.RatingDto
import com.github.quillraven.toilapp.model.dto.UserDto
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

const val RATINGS_COLLECTION_NAME = "ratings"

@Document(collection = RATINGS_COLLECTION_NAME)
data class Rating(
    @Id
    val id: ObjectId = ObjectId(),
    val toiletId: ObjectId = ObjectId(),
    val userRef: ObjectId = ObjectId(),
    val value: Int = 0
) {
    fun createRatingDto(user: UserDto) = RatingDto(
        id = id.toHexString(),
        user = user,
        value = value
    )
}

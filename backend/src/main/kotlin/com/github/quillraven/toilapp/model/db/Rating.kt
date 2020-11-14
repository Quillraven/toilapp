package com.github.quillraven.toilapp.model.db

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "ratings")
data class Rating(
    @Id
    val id: ObjectId = ObjectId(),
    val userRef: ObjectId = ObjectId(),
    val value: Double = 1.0
)

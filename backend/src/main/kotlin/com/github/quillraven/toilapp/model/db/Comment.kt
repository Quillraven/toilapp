package com.github.quillraven.toilapp.model.db

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "comments")
data class Comment(
    @Id
    val id: ObjectId = ObjectId(),
    val userRef: ObjectId = ObjectId(),
    val date: Date = Date(),
    val text: String = "",
)

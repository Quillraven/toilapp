package com.github.quillraven.toilapp.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "comments")
data class Comment(
    @Id
    val id: ObjectId = ObjectId.get(),
    @DBRef
    val user: User = User(),
    val date: Date = Date(),
    val text: String = "",
)

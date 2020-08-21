package com.github.quillraven.toilapp.model

import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class Comment(
    val user: User,
    val date: Date,
    val text: String
)

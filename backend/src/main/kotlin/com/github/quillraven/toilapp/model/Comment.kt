package com.github.quillraven.toilapp.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class Comment(
    @Id
    val id: String = "",
    val user: User = User(),
    val date: Date = Date(),
    val text: String = ""
)

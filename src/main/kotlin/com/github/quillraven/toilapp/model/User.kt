package com.github.quillraven.toilapp.model

import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User(
    val id: String,
    val name: String,
    val email: String
)

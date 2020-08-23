package com.github.quillraven.toilapp.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User(
    @Id
    val id: String = "",
    val name: String = "",
    val email: String = ""
)

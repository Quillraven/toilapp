package com.github.quillraven.toilapp.model.dto

import java.util.*

data class CommentDto(
    val id: String,
    val user: UserDto,
    val date: Date,
    val text: String,
)

package com.github.quillraven.toilapp.model.dto

import java.time.LocalDateTime

data class CommentDto(
    val id: String,
    val user: UserDto,
    val localDateTime: LocalDateTime,
    val text: String
)

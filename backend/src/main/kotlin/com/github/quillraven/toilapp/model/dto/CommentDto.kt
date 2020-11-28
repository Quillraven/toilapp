package com.github.quillraven.toilapp.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class CommentDto(
    val id: String,
    val user: UserDto,
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    val date: LocalDateTime,
    val text: String
)

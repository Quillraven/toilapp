package com.github.quillraven.toilapp.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.util.*

data class CommentDto(
    val id: String,
    val user: UserDto,
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm")
    val date: Date,
    val text: String
)

package com.github.quillraven.toilapp.model.dto

data class CreateUpdateCommentDto(
    val commentId: String = "",
    val toiletId: String = "",
    val text: String = ""
)

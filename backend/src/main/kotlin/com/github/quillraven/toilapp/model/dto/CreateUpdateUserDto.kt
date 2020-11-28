package com.github.quillraven.toilapp.model.dto

data class CreateUpdateUserDto(
    val id: String = "",
    val name: String,
    val email: String
)

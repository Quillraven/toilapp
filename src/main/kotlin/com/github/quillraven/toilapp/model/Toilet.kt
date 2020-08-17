package com.github.quillraven.toilapp.model

data class Toilet(
    val title: String,
    val description: String,
    val preview: String,
    val rating: Int,
    val comments: Array<Comment>,
    val location: String,
    val images: Array<String>
)

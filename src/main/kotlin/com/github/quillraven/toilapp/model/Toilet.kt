package com.github.quillraven.toilapp.model

import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Toilet(
    val title: String? = null,
    val description: String? = null,
    val preview: String? = null,
    val rating: Int? = null,
    val comments: Array<Comment>? = null,
    val location: String? = null,
    val images: Array<String>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Toilet

        if (title != other.title) return false
        if (location != other.location) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + location.hashCode()
        return result
    }
}

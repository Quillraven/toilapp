package com.github.quillraven.toilapp.model.db

import org.bson.types.ObjectId

data class ImageMetadata(
    val toiletId: ObjectId,
    val preview: Boolean
)

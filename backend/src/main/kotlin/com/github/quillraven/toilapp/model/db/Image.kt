package com.github.quillraven.toilapp.model.db

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

const val IMAGES_COLLECTION_NAME = "images.files"

@Document(collection = IMAGES_COLLECTION_NAME)
data class Image(
    @Id
    val id: ObjectId,
    val metadata: ImageMetadata
)

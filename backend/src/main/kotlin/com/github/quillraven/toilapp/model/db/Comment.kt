package com.github.quillraven.toilapp.model.db

import com.github.quillraven.toilapp.model.dto.CommentDto
import com.github.quillraven.toilapp.model.dto.UserDto
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

const val COMMENTS_COLLECTION_NAME = "comments"

@Document(collection = COMMENTS_COLLECTION_NAME)
data class Comment(
    @Id
    val id: ObjectId = ObjectId(),
    val toiletId: ObjectId = ObjectId(),
    val userRef: ObjectId = ObjectId(),
    @LastModifiedDate
    val localDateTime: LocalDateTime = LocalDateTime.now(),
    val text: String = ""
) {
    fun createCommentDto(user: UserDto) = CommentDto(
        id = id.toHexString(),
        user = user,
        localDateTime = localDateTime,
        text = text
    )
}

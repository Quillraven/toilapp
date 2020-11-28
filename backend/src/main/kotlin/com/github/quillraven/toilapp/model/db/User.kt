package com.github.quillraven.toilapp.model.db

import com.github.quillraven.toilapp.model.dto.UserDto
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
data class User(
    @Id
    val id: ObjectId = ObjectId(),
    val name: String = "",
    val email: String = ""
) {
    fun createUserDto() = UserDto(
        id = id.toHexString(),
        email = email,
        name = name
    )
}

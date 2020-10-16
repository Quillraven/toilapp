package com.github.quillraven.toilapp.repository

import com.github.quillraven.toilapp.model.db.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : ReactiveMongoRepository<User, ObjectId>

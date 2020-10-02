package com.github.quillraven.toilapp.repository

import com.github.quillraven.toilapp.model.User
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface UserRepository : ReactiveMongoRepository<User, String>
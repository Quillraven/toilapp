package com.github.quillraven.toilapp.repository

import com.github.quillraven.toilapp.model.Comment
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository : ReactiveMongoRepository<Comment, String>

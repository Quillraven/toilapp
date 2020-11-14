package com.github.quillraven.toilapp.repository

import com.github.quillraven.toilapp.model.db.Rating
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository


@Repository
interface RatingRepository : ReactiveMongoRepository<Rating, ObjectId>

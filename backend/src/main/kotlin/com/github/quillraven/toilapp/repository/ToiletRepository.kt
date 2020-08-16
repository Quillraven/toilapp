package com.github.quillraven.toilapp.repository

import com.github.quillraven.toilapp.model.Toilet
import org.springframework.data.mongodb.repository.MongoRepository

interface ToiletRepository : MongoRepository<Toilet, String> {
    fun findByLocation(location: String): Array<Toilet>
}

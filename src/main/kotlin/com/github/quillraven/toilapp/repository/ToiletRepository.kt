package com.github.quillraven.toilapp.repository

import com.github.quillraven.toilapp.model.Toilet
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ToiletRepository : ReactiveMongoRepository<Toilet, String>

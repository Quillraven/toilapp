package com.github.quillraven.toilapp.repository

import com.github.quillraven.toilapp.model.Toilet
import org.springframework.data.geo.Distance
import org.springframework.data.geo.GeoResult
import org.springframework.data.geo.Point
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux


@Repository
interface ToiletRepository : ReactiveMongoRepository<Toilet, String> {
    fun findByLocationNear(location: Point, distance: Distance): Flux<GeoResult<Toilet>>
}



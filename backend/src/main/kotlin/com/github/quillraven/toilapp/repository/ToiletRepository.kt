package com.github.quillraven.toilapp.repository

import com.github.quillraven.toilapp.model.Toilet
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ToiletRepository : ReactiveMongoRepository<Toilet, String> {
    @Query(
        value = """
        {
            "location": {
                "${'$'}near" : {
                    "${'$'}geometry" : {
                        "type" : "Point",
                        "coordinates" : [?0, ?1]
                    },
                    "${'$'}maxDistance" : ?2
                }
            }
        }
    """
    )
    fun getNearbyToilets(
        x: Double,
        y: Double,
        maxDistanceInMeters: Double
    ): Flux<Toilet>
}

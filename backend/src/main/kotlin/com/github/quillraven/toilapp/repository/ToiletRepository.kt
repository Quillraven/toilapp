package com.github.quillraven.toilapp.repository

import com.github.quillraven.toilapp.model.db.DistanceInfo
import com.github.quillraven.toilapp.model.db.TOILETS_COLLECTION_NAME
import com.github.quillraven.toilapp.model.db.Toilet
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.geo.Distance
import org.springframework.data.geo.GeoResult
import org.springframework.data.geo.Metrics
import org.springframework.data.geo.Point
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.NearQuery
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@Repository
interface ToiletRepository : ReactiveMongoRepository<Toilet, ObjectId>, CustomToiletRepository {
    @Suppress("SpringDataRepositoryMethodReturnTypeInspection", "SpringDataRepositoryMethodParametersInspection")
    fun findByLocationNear(location: Point, distance: Distance): Flux<GeoResult<Toilet>>
}

interface CustomToiletRepository {
    fun getDistanceBetween(toiletId: ObjectId, point: Point): Mono<DistanceInfo>
}

class CustomToiletRepositoryImpl(
    @Autowired private val mongoTemplate: ReactiveMongoTemplate
) : CustomToiletRepository {
    override fun getDistanceBetween(toiletId: ObjectId, point: Point): Mono<DistanceInfo> {
        val nearQuery = NearQuery
            .near(point, Metrics.KILOMETERS)
            .spherical(true)
            // filter by toilet id
            .query(Query(Criteria(ID_FIELD_NAME).`is`(toiletId)))

        val aggregation = Aggregation.newAggregation(Aggregation.geoNear(nearQuery, DistanceInfo::distance.name))

        return mongoTemplate
            .aggregate(aggregation, TOILETS_COLLECTION_NAME, DistanceInfo::class.java)
            .single(DistanceInfo())
            // transform distance correctly to meters
            .map { it.copy(distance = it.distance * 1000) }
    }

    companion object {
        private const val ID_FIELD_NAME = "_id"
    }
}



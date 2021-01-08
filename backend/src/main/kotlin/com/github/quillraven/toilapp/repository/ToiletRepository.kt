package com.github.quillraven.toilapp.repository

import com.github.quillraven.toilapp.model.db.DistanceInfo
import com.github.quillraven.toilapp.model.db.TOILETS_COLLECTION_NAME
import com.github.quillraven.toilapp.model.db.Toilet
import com.github.quillraven.toilapp.model.db.ToiletDistanceInfo
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.geo.Metrics
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.NearQuery
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface ToiletRepository : ReactiveMongoRepository<Toilet, ObjectId>, CustomToiletRepository

interface CustomToiletRepository {
    fun getDistanceBetween(toiletId: ObjectId, point: GeoJsonPoint): Mono<DistanceInfo>

    fun getNearbyToilets(
        location: GeoJsonPoint,
        radiusInKm: Double,
        maxToiletsToLoad: Long,
        minDistanceInKm: Double = 0.0,
        toiletIdsToExclude: Set<String> = setOf()
    ): Flux<ToiletDistanceInfo>
}

class CustomToiletRepositoryImpl(
    @Autowired private val mongoTemplate: ReactiveMongoTemplate
) : CustomToiletRepository {
    override fun getDistanceBetween(toiletId: ObjectId, point: GeoJsonPoint): Mono<DistanceInfo> {
        val nearQuery = NearQuery
            .near(point, Metrics.KILOMETERS)
            .spherical(true)
            // filter by toilet id
            .query(Query(Criteria(ID_FIELD_NAME).`is`(toiletId)))

        val aggregation = Aggregation.newAggregation(Aggregation.geoNear(nearQuery, DistanceInfo::distance.name))

        return mongoTemplate
            .aggregate(aggregation, TOILETS_COLLECTION_NAME, DistanceInfo::class.java)
            .single(DistanceInfo())
    }

    override fun getNearbyToilets(
        location: GeoJsonPoint,
        radiusInKm: Double,
        maxToiletsToLoad: Long,
        minDistanceInKm: Double,
        toiletIdsToExclude: Set<String>
    ): Flux<ToiletDistanceInfo> {
        val nearQuery = NearQuery
            .near(location, Metrics.KILOMETERS)
            .maxDistance(radiusInKm)
            .limit(maxToiletsToLoad)
            .spherical(true)

        if (minDistanceInKm > 0) {
            nearQuery.minDistance(minDistanceInKm)
        }
        if (toiletIdsToExclude.isNotEmpty()) {
            nearQuery.query(Query(Criteria(ID_FIELD_NAME).nin(toiletIdsToExclude)))
        }

        val aggregation = Aggregation.newAggregation(Aggregation.geoNear(nearQuery, ToiletDistanceInfo::distance.name))

        return mongoTemplate.aggregate(aggregation, TOILETS_COLLECTION_NAME, ToiletDistanceInfo::class.java)
    }

    companion object {
        private const val ID_FIELD_NAME = "_id"
    }
}



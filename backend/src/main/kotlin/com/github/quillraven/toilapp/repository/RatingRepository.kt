package com.github.quillraven.toilapp.repository

import com.github.quillraven.toilapp.model.db.RATINGS_COLLECTION_NAME
import com.github.quillraven.toilapp.model.db.Rating
import com.github.quillraven.toilapp.model.db.RatingInfo
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono


@Repository
interface RatingRepository : ReactiveMongoRepository<Rating, ObjectId>, CustomRatingRepository {
    fun getByToiletIdAndUserRef(toiletId: ObjectId, userRef: ObjectId): Mono<Rating>
    fun deleteByToiletId(toiletId: ObjectId): Mono<Void>
}

interface CustomRatingRepository {
    fun getAverageRating(toiletId: ObjectId): Mono<Double>
}

class CustomRatingRepositoryImpl(
    @Autowired private val mongoTemplate: ReactiveMongoTemplate
) : CustomRatingRepository {
    override fun getAverageRating(toiletId: ObjectId): Mono<Double> {
        val aggregation = Aggregation.newAggregation(
            // filter by toilet id
            Aggregation.match(Criteria(Rating::toiletId.name).`is`(toiletId)),
            // group by toilet id and user sum and count to calculate average rating
            Aggregation.group(Rating::toiletId.name)
                .count().`as`(RatingInfo::numRatings.name)
                .sum(Rating::value.name).`as`(RatingInfo::total.name)
        )

        return mongoTemplate
            .aggregate(aggregation, RATINGS_COLLECTION_NAME, RatingInfo::class.java)
            .single(RatingInfo())
            .map {
                when {
                    it.numRatings > 0 -> it.total.toDouble() / it.numRatings
                    else -> 0.0
                }
            }
    }
}

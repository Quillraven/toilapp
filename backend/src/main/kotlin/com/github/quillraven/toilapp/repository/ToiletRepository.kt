package com.github.quillraven.toilapp.repository

import com.github.quillraven.toilapp.model.db.TOILETS_COLLECTION_NAME
import com.github.quillraven.toilapp.model.db.Toilet
import com.github.quillraven.toilapp.model.db.ToiletCommentInfo
import com.github.quillraven.toilapp.model.db.ToiletRatingInfo
import com.github.quillraven.toilapp.model.db.ToiletRatings
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.geo.Distance
import org.springframework.data.geo.GeoResult
import org.springframework.data.geo.Point
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@Repository
interface ToiletRepository : ReactiveMongoRepository<Toilet, ObjectId>, CustomToiletRepository {
    @Suppress("SpringDataRepositoryMethodReturnTypeInspection", "SpringDataRepositoryMethodParametersInspection")
    fun findByLocationNear(location: Point, distance: Distance): Flux<GeoResult<Toilet>>

    fun findByPreviewID(id: ObjectId): Flux<Toilet>
}

interface CustomToiletRepository {
    fun addComment(toiletId: ObjectId, commentId: ObjectId): Mono<Toilet>
    fun removeComment(toiletId: ObjectId, commentId: ObjectId): Mono<Toilet>
    fun getCommentInfo(toiletId: ObjectId): Flux<ToiletCommentInfo>
    fun findByCommentRefsContains(commentId: ObjectId): Flux<Toilet>
    fun addRating(toiletId: ObjectId, ratingId: ObjectId, ratingValue: Int): Mono<Toilet>
    fun updateRating(toiletId: ObjectId, oldValue: Int, newValue: Int): Mono<Toilet>
    fun removeRating(toiletId: ObjectId, ratingId: ObjectId, ratingValue: Int): Mono<Toilet>
    fun getRatingInfo(toiletId: ObjectId): Mono<ToiletRatingInfo>
    fun getRatings(toiletId: ObjectId): Flux<ToiletRatings>
    fun findByRatingRefsContains(ratingId: ObjectId): Flux<Toilet>
    fun removePreviewID(toiletId: ObjectId): Mono<Toilet>
}

class CustomToiletRepositoryImpl(
    @Autowired private val mongoTemplate: ReactiveMongoTemplate
) : CustomToiletRepository {
    override fun addComment(toiletId: ObjectId, commentId: ObjectId): Mono<Toilet> {
        val query = Query(Criteria(ID_FIELD_NAME).`is`(toiletId))

        val update = Update().addToSet(ToiletCommentInfo::commentRefs.name, commentId)

        return mongoTemplate.findAndModify(query, update, Toilet::class.java)
    }

    override fun removeComment(toiletId: ObjectId, commentId: ObjectId): Mono<Toilet> {
        val query = Query(Criteria(ID_FIELD_NAME).`is`(toiletId))

        val update = Update().pull(ToiletCommentInfo::commentRefs.name, commentId)

        return mongoTemplate.findAndModify(query, update, Toilet::class.java)
    }

    override fun getCommentInfo(toiletId: ObjectId): Flux<ToiletCommentInfo> {
        val query = Query(
            Criteria(ID_FIELD_NAME).`is`(toiletId)
                .andOperator(Criteria(ToiletCommentInfo::commentRefs.name).exists(true))
        )

        return mongoTemplate
            .find(query, ToiletCommentInfo::class.java, TOILETS_COLLECTION_NAME)
    }

    override fun findByCommentRefsContains(commentId: ObjectId): Flux<Toilet> {
        val query = Query(Criteria(ToiletCommentInfo::commentRefs.name).`is`(commentId))

        return mongoTemplate.find(query, Toilet::class.java, TOILETS_COLLECTION_NAME)
    }

    override fun addRating(toiletId: ObjectId, ratingId: ObjectId, ratingValue: Int): Mono<Toilet> {
        val query = Query(Criteria(ID_FIELD_NAME).`is`(toiletId))

        val update = Update()
            .addToSet(ToiletRatings::ratingRefs.name, ratingId)
            .inc(Toilet::totalRating.name, ratingValue)

        return mongoTemplate.findAndModify(query, update, Toilet::class.java)
    }

    override fun updateRating(toiletId: ObjectId, oldValue: Int, newValue: Int): Mono<Toilet> {
        val query = Query(Criteria(ID_FIELD_NAME).`is`(toiletId))

        val update = Update()
            .inc(Toilet::totalRating.name, -oldValue + newValue)

        return mongoTemplate.findAndModify(query, update, Toilet::class.java)
    }

    override fun removeRating(toiletId: ObjectId, ratingId: ObjectId, ratingValue: Int): Mono<Toilet> {
        val query = Query(Criteria(ID_FIELD_NAME).`is`(toiletId))

        val update = Update()
            .pull(ToiletRatings::ratingRefs.name, ratingId)
            .inc(Toilet::totalRating.name, -ratingValue)

        return mongoTemplate.findAndModify(query, update, Toilet::class.java)
    }

    override fun getRatingInfo(toiletId: ObjectId): Mono<ToiletRatingInfo> {
        val aggregation = Aggregation.newAggregation(
            Aggregation.match(
                Criteria(ID_FIELD_NAME).`is`(toiletId)
                    .andOperator(Criteria(ToiletRatings::ratingRefs.name).exists(true))
            ),
            Aggregation.project()
                .andInclude(Toilet::totalRating.name)
                .and(ToiletRatings::ratingRefs.name).size().`as`(ToiletRatingInfo::numRatings.name)
        )

        return mongoTemplate
            .aggregate(aggregation, TOILETS_COLLECTION_NAME, ToiletRatingInfo::class.java)
            .single(ToiletRatingInfo())
    }

    override fun getRatings(toiletId: ObjectId): Flux<ToiletRatings> {
        val query = Query(
            Criteria(ID_FIELD_NAME).`is`(toiletId)
                .andOperator(Criteria(ToiletRatings::ratingRefs.name).exists(true))
        )

        return mongoTemplate
            .find(query, ToiletRatings::class.java, TOILETS_COLLECTION_NAME)
    }

    override fun findByRatingRefsContains(ratingId: ObjectId): Flux<Toilet> {
        val query = Query(Criteria(ToiletRatings::ratingRefs.name).`is`(ratingId))

        return mongoTemplate.find(query, Toilet::class.java, TOILETS_COLLECTION_NAME)
    }

    override fun removePreviewID(toiletId: ObjectId): Mono<Toilet> {
        val query = Query(Criteria(ID_FIELD_NAME).`is`(toiletId))

        val update = Update().set(Toilet::previewID.name, null)

        return mongoTemplate.findAndModify(query, update, Toilet::class.java)
    }

    companion object {
        private const val ID_FIELD_NAME = "_id"
    }
}



package com.github.quillraven.toilapp.repository

import com.github.quillraven.toilapp.model.db.DistanceInfo
import com.github.quillraven.toilapp.model.db.TOILETS_COLLECTION_NAME
import io.mockk.every
import io.mockk.mockk
import org.bson.types.ObjectId
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.springframework.data.geo.Point
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

object ToiletRepositorySpec : Spek({
    val reactiveMongoTemplate: ReactiveMongoTemplate by memoized { mockk<ReactiveMongoTemplate>() }
    val toiletRepository: CustomToiletRepository by memoized { CustomToiletRepositoryImpl(reactiveMongoTemplate) }

    describe("CustomToiletRepository") {
        it("should return DistanceInfo of 100") {
            val toiletId = ObjectId()
            val point = Point(0.0, 0.0)
            val distanceInfo = DistanceInfo(100.0)
            every {
                reactiveMongoTemplate.aggregate(
                    any<Aggregation>(),
                    TOILETS_COLLECTION_NAME,
                    DistanceInfo::class.java
                )
            } returns Flux.just(distanceInfo)

            StepVerifier
                .create(toiletRepository.getDistanceBetween(toiletId, point))
                .expectNext(distanceInfo.copy(distance = distanceInfo.distance * 1000))
                .expectComplete()
                .verify()
        }

        it("should return DistanceInfo of 0") {
            val toiletId = ObjectId()
            val point = Point(0.0, 0.0)
            every {
                reactiveMongoTemplate.aggregate(
                    any<Aggregation>(),
                    TOILETS_COLLECTION_NAME,
                    DistanceInfo::class.java
                )
            } returns Flux.empty()

            StepVerifier
                .create(toiletRepository.getDistanceBetween(toiletId, point))
                .expectNext(DistanceInfo(0.0))
                .expectComplete()
                .verify()
        }
    }
})

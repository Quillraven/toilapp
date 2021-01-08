package com.github.quillraven.toilapp.repository

import com.github.quillraven.toilapp.model.db.RATINGS_COLLECTION_NAME
import com.github.quillraven.toilapp.model.db.RatingInfo
import com.github.quillraven.toilapp.model.db.Toilet
import io.mockk.every
import io.mockk.mockk
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

object RatingRepositorySpec : Spek({
    val reactiveMongoTemplate: ReactiveMongoTemplate by memoized { mockk<ReactiveMongoTemplate>() }
    val ratingRepository: CustomRatingRepository by memoized { CustomRatingRepositoryImpl(reactiveMongoTemplate) }

    describe("CustomRatingRepository") {
        it("should return 2.5") {
            val toilet = Toilet()
            every {
                reactiveMongoTemplate.aggregate(
                    any<Aggregation>(),
                    RATINGS_COLLECTION_NAME,
                    RatingInfo::class.java
                )
            } returns Flux.just(RatingInfo(5, 2))

            StepVerifier
                .create(ratingRepository.getAverageRating(toilet.id))
                .expectNext(2.5)
                .expectComplete()
                .verify()
        }

        it("should return 0.0") {
            val toilet = Toilet()
            every {
                reactiveMongoTemplate.aggregate(
                    any<Aggregation>(),
                    RATINGS_COLLECTION_NAME,
                    RatingInfo::class.java
                )
            } returns Flux.empty()

            StepVerifier
                .create(ratingRepository.getAverageRating(toilet.id))
                .expectNext(0.0)
                .expectComplete()
                .verify()
        }
    }
})

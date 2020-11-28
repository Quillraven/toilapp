package com.github.quillraven.toilapp.repository

import com.github.quillraven.toilapp.model.db.COMMENTS_COLLECTION_NAME
import com.github.quillraven.toilapp.model.db.Comment
import io.mockk.every
import io.mockk.mockk
import org.bson.types.ObjectId
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

object CommentRepositorySpec : Spek({
    val reactiveMongoTemplate: ReactiveMongoTemplate by memoized { mockk<ReactiveMongoTemplate>() }
    val commentRepository: CustomCommentRepository by memoized { CustomCommentRepositoryImpl(reactiveMongoTemplate) }

    describe("CustomCommentRepository") {
        it("should return 42") {
            val toiletId = ObjectId()
            every {
                reactiveMongoTemplate.count(
                    Query(Criteria(Comment::toiletId.name).`is`(toiletId)),
                    COMMENTS_COLLECTION_NAME
                )
            } returns Mono.just(42L)

            StepVerifier
                .create(commentRepository.getNumComments(toiletId))
                .expectNext(42L)
                .expectComplete()
                .verify()
        }
    }
})

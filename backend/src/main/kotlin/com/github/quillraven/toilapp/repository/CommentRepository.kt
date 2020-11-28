package com.github.quillraven.toilapp.repository

import com.github.quillraven.toilapp.model.db.COMMENTS_COLLECTION_NAME
import com.github.quillraven.toilapp.model.db.Comment
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface CommentRepository : ReactiveMongoRepository<Comment, ObjectId>, CustomCommentRepository {
    fun deleteByToiletId(toiletId: ObjectId): Mono<Void>

    fun findAllByToiletIdOrderByDateDesc(toiletId: ObjectId, pageable: Pageable): Flux<Comment>
}

interface CustomCommentRepository {
    fun getNumComments(toiletId: ObjectId): Mono<Long>
}

class CustomCommentRepositoryImpl(
    @Autowired private val mongoTemplate: ReactiveMongoTemplate
) : CustomCommentRepository {
    override fun getNumComments(toiletId: ObjectId): Mono<Long> {
        val query = Query(Criteria(Comment::toiletId.name).`is`(toiletId))

        return mongoTemplate.count(query, COMMENTS_COLLECTION_NAME)
    }
}

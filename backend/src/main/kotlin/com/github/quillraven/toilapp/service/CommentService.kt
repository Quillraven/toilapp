package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.CommentDoesNotExistException
import com.github.quillraven.toilapp.model.Comment
import com.github.quillraven.toilapp.model.User
import com.github.quillraven.toilapp.repository.CommentRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

interface CommentService {
    fun create(user: User, text: String): Mono<Comment>
    fun update(id: ObjectId, text: String): Mono<Comment>
    fun getById(id: ObjectId): Mono<Comment>
    fun delete(id: ObjectId): Mono<Void>
}

@Service
class DefaultCommentService(
    @Autowired private val commentRepository: CommentRepository
) : CommentService {
    override fun create(user: User, text: String): Mono<Comment> {
        LOG.debug("Create comment |$text| for user |$user|")
        return commentRepository.save(Comment(user = user, text = text))
    }

    override fun update(id: ObjectId, text: String): Mono<Comment> {
        LOG.debug("update: (commentId=$id, text=$text)")
        return getById(id)
            .flatMap { commentRepository.save(it.copy(id = id, text = text)) }
    }

    override fun getById(id: ObjectId): Mono<Comment> {
        LOG.debug("getById: (id=$id)")
        return commentRepository
            .findById(id.toHexString())
            .switchIfEmpty(Mono.error(CommentDoesNotExistException(id.toHexString())))
    }

    override fun delete(id: ObjectId): Mono<Void> {
        LOG.debug("delete: (id=$id)")
        return getById(id)
            .flatMap { commentRepository.deleteById(id.toHexString()) }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(CommentService::class.java)
    }
}

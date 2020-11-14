package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.CommentDoesNotExistException
import com.github.quillraven.toilapp.model.db.Comment
import com.github.quillraven.toilapp.model.db.User
import com.github.quillraven.toilapp.model.dto.CommentDto
import com.github.quillraven.toilapp.model.dto.CreateUpdateCommentDto
import com.github.quillraven.toilapp.model.dto.UserDto
import com.github.quillraven.toilapp.repository.CommentRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

interface CommentService {
    fun createCommentDto(comment: Comment, user: User? = null): CommentDto
    fun create(userId: ObjectId, text: String): Mono<CommentDto>
    fun update(createUpdateCommentDto: CreateUpdateCommentDto): Mono<CommentDto>
    fun getById(id: String): Mono<Comment>
    fun delete(id: String): Mono<Void>
}

@Service
class DefaultCommentService(
    @Autowired private val commentRepository: CommentRepository
) : CommentService {

    /**
     * Returns a [CommentDto] instance out of the given [comment].
     * The [UserDto] of the comment only contains the id. Name and email
     * need to be fetched separately.
     */
    override fun createCommentDto(comment: Comment, user: User?) = CommentDto(
        comment.id.toHexString(),
        UserDto(comment.userRef.toHexString(), user?.name ?: "", ""),
        comment.date,
        comment.text
    )

    override fun create(userId: ObjectId, text: String): Mono<CommentDto> {
        LOG.debug("create: (userId=$userId, text=$text)")
        return commentRepository
            .save(Comment(userRef = userId, text = text))
            .map { createCommentDto(it) }
    }

    override fun update(createUpdateCommentDto: CreateUpdateCommentDto): Mono<CommentDto> {
        LOG.debug("update: (commentId=${createUpdateCommentDto.commentId}, text=${createUpdateCommentDto.text})")
        return getById(createUpdateCommentDto.commentId)
            .flatMap {
                commentRepository.save(
                    it.copy(
                        id = ObjectId(createUpdateCommentDto.commentId),
                        text = createUpdateCommentDto.text,
                        date = Date()
                    )
                )
            }
            .map { createCommentDto(it) }
    }

    override fun getById(id: String): Mono<Comment> {
        LOG.debug("getById: (id=$id)")
        return commentRepository
            .findById(ObjectId(id))
            .switchIfEmpty(Mono.error(CommentDoesNotExistException(id)))
    }

    override fun delete(id: String): Mono<Void> {
        LOG.debug("delete: (id=$id)")
        return commentRepository.findById(ObjectId(id))
            .flatMap { commentRepository.deleteById(ObjectId(id)) }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultCommentService::class.java)
    }
}


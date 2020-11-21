package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.CommentDoesNotExistException
import com.github.quillraven.toilapp.model.db.Comment
import com.github.quillraven.toilapp.model.db.User
import com.github.quillraven.toilapp.model.dto.CommentDto
import com.github.quillraven.toilapp.model.dto.CreateUpdateCommentDto
import com.github.quillraven.toilapp.model.dto.UserDto
import com.github.quillraven.toilapp.repository.CommentRepository
import com.github.quillraven.toilapp.repository.ToiletRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.util.*

interface CommentService {
    fun createCommentDto(comment: Comment, user: User? = null): CommentDto
    fun create(userId: ObjectId, text: String): Mono<Comment>
    fun getById(id: String): Mono<Comment>
    fun update(createUpdateCommentDto: CreateUpdateCommentDto): Mono<CommentDto>
    fun delete(id: String): Mono<Void>
}

@Service
class DefaultCommentService(
    @Autowired private val commentRepository: CommentRepository,
    @Autowired private val toiletRepository: ToiletRepository
) : CommentService {

    /**
     * Returns a [CommentDto] instance out of the given [comment].
     * The [UserDto] of the comment only contains the id. Name and email
     * need to be fetched separately.
     */
    override fun createCommentDto(comment: Comment, user: User?) = CommentDto(
        comment.id.toHexString(),
        UserDto(comment.userRef.toHexString(), user?.name ?: "", user?.email ?: ""),
        comment.date,
        comment.text
    )

    @Transactional
    override fun create(userId: ObjectId, text: String): Mono<Comment> {
        LOG.debug("create: (userId=$userId, text=$text)")

        return commentRepository.save(
            Comment(
                userRef = userId,
                text = text
            )
        )
    }

    override fun getById(id: String): Mono<Comment> {
        LOG.debug("getById: (id=$id)")
        return commentRepository
            .findById(ObjectId(id))
            .switchIfEmpty(Mono.error(CommentDoesNotExistException(id)))
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

    @Transactional
    override fun delete(id: String): Mono<Void> {
        LOG.debug("delete: (id=$id)")
        val commentId = ObjectId(id)

        return toiletRepository.findByCommentRefsContains(commentId)
            // remove comment from any toilets
            .flatMap {
                LOG.debug("Deleting comment from toilet $it")
                toiletRepository.removeComment(it.id, commentId)
            }
            .collectList()
            // remove comment itself
            .flatMap {
                LOG.debug("Deleting comment '$id'")
                commentRepository.deleteById(commentId)
            }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultCommentService::class.java)
    }
}


package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.CommentDoesNotExistException
import com.github.quillraven.toilapp.model.db.Comment
import com.github.quillraven.toilapp.model.db.User
import com.github.quillraven.toilapp.model.dto.CommentDto
import com.github.quillraven.toilapp.model.dto.UserDto
import com.github.quillraven.toilapp.repository.CommentRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URLDecoder
import java.util.*

interface CommentService {
    fun create(userId: String, text: String): Mono<CommentDto>
    fun update(id: String, text: String): Mono<CommentDto>
    fun getById(id: String): Mono<Comment>
    fun delete(id: String): Mono<Void>
    fun createAndLink(userId: String, text: String, toiletId: String): Mono<CommentDto>
    fun getComments(toiletId: String): Flux<CommentDto>
}

@Service
class DefaultCommentService(
    @Autowired private val commentRepository: CommentRepository,
    @Autowired private val userService: UserService,
    @Autowired private val toiletService: ToiletService,
) : CommentService {
    /**
     * Returns a [CommentDto] instance out of the given [comment].
     * The [UserDto] of the comment only contains the id. Name and email
     * need to be fetched separately.
     */
    private fun createCommentDto(comment: Comment, user: User? = null) = CommentDto(
        comment.id.toHexString(),
        UserDto(comment.userRef.toHexString(), user?.name ?: "", ""),
        comment.date,
        comment.text
    )

    override fun create(userId: String, text: String): Mono<CommentDto> {
        LOG.debug("create: (userId=$userId, text=$text)")
        return commentRepository
            .save(Comment(userRef = ObjectId(userId), text = text))
            .map { createCommentDto(it) }
    }

    override fun update(id: String, text: String): Mono<CommentDto> {
        LOG.debug("update: (commentId=$id, text=$text)")
        return getById(id)
            .flatMap { commentRepository.save(it.copy(id = ObjectId(id), text = text, date = Date())) }
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
        return getById(id)
            .flatMap { commentRepository.deleteById(ObjectId(id)) }
        // TODO also remove from toilets
    }

    override fun createAndLink(userId: String, text: String, toiletId: String): Mono<CommentDto> {
        LOG.debug("createAndLink: (userId=$userId, text=$text, toiletId=$toiletId)")
        return userService
            // check if user is valid
            .getById(userId)
            // if yes -> create new comment
            .flatMap {
                LOG.debug("Found user to enter comment")
                // TODO how to replace blocking decode call?
                create(it.id.toHexString(), URLDecoder.decode(text, "UTF-8"))
            }
            // if it was created successfully -> link it to toilet
            .zipWith(toiletService.getById(toiletId))
            .flatMap { tuple ->
                val comment = tuple.t1
                val toilet = tuple.t2

                LOG.debug("Found toilet to enter comment")

                toiletService.addComment(comment.id, toilet)
            }
            .flatMap { getById(it.commentRefs.last().toHexString()) }
            .map { createCommentDto(it) }
    }

    override fun getComments(toiletId: String): Flux<CommentDto> {
        LOG.debug("getComments: (toiletId=$toiletId)")
        return toiletService
            // get toilet
            .getById(toiletId)
            // retrieve comments by IDs
            .map { it.commentRefs }
            .flatMapMany { Flux.fromIterable(it) }
            .flatMap { getById(it.toHexString()) }
            .flatMap { comment ->
                userService.getById(comment.userRef.toHexString())
                    .map { createCommentDto(comment, it) }
            }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultCommentService::class.java)
    }
}


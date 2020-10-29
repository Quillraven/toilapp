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
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

interface CommentService {
    fun create(userId: ObjectId, text: String): Mono<CommentDto>
    fun update(createUpdateCommentDto: CreateUpdateCommentDto): Mono<CommentDto>
    fun getById(id: String): Mono<Comment>
    fun deleteAndRemove(id: String): Mono<Void>
    fun delete(id: String): Mono<Void>
    fun createAndLink(createUpdateCommentDto: CreateUpdateCommentDto): Mono<CommentDto>
    fun getComments(toiletId: String): Flux<CommentDto>
}

@Service
class DefaultCommentService(
    @Autowired private val commentRepository: CommentRepository
) : CommentService {
    lateinit var userService: UserService
        @Autowired set

    lateinit var toiletService: ToiletService
        @Autowired set

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

    override fun deleteAndRemove(id: String): Mono<Void> {
        LOG.debug("deleteAndRemove: (id=$id)")
        return toiletService
            .getByCommentId(id)
            // remove comment from any toilet where it is referenced
            .flatMap { toiletService.removeComment(id, it) }
            // finally delete it
            .then(delete(id))
    }

    override fun delete(id: String): Mono<Void> {
        LOG.debug("delete: (id=$id)")
        return commentRepository.findById(ObjectId(id))
            .flatMap { commentRepository.deleteById(ObjectId(id)) }
    }

    override fun createAndLink(createUpdateCommentDto: CreateUpdateCommentDto): Mono<CommentDto> {
        val userId = userService.getCurrentUser()
        LOG.debug("createAndLink: (userId=$userId, text=${createUpdateCommentDto.text}, toiletId=${createUpdateCommentDto.toiletId})")
        return userService
            // check if user is valid
            .getById(userId)
            // if yes -> create new comment
            .flatMap {
                LOG.debug("Found user '$userId' for comment '${createUpdateCommentDto.text}'")
                create(userId, createUpdateCommentDto.text)
            }
            // if it was created successfully -> link it to toilet
            .zipWith(toiletService.getById(createUpdateCommentDto.toiletId))
            .flatMap { tuple ->
                val comment = tuple.t1
                val toilet = tuple.t2

                LOG.debug("Found toilet '${toilet.id} for comment '$comment")

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
            // retrieve comments by IDsh
            .map { it.commentRefs }
            .flatMapMany { Flux.fromIterable(it) }
            .flatMap { getById(it.toHexString()) }
            // and add user name information to each comment
            .flatMap { Mono.just(it).zipWith(userService.getById(it.userRef.toHexString())) }
            .map { createCommentDto(it.t1, it.t2) }
            .sort { o1, o2 -> o2.date.compareTo(o1.date) }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultCommentService::class.java)
    }
}


package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.CommentDoesNotExistException
import com.github.quillraven.toilapp.InvalidIdException
import com.github.quillraven.toilapp.ToiletDoesNotExistException
import com.github.quillraven.toilapp.model.db.Comment
import com.github.quillraven.toilapp.model.dto.CommentDto
import com.github.quillraven.toilapp.model.dto.CreateUpdateCommentDto
import com.github.quillraven.toilapp.repository.CommentRepository
import com.github.quillraven.toilapp.repository.ToiletRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

interface CommentService {
    fun create(createUpdateCommentDto: CreateUpdateCommentDto): Mono<CommentDto>
    fun update(createUpdateCommentDto: CreateUpdateCommentDto): Mono<CommentDto>
    fun getNumComments(toiletId: ObjectId): Mono<Long>
    fun getComments(toiletId: String, page: Int, numComments: Int): Flux<CommentDto>
    fun delete(id: String): Mono<Void>
    fun deleteByToiletId(toiletId: ObjectId): Mono<Void>
}

@Service
class DefaultCommentService(
    @Autowired private val commentRepository: CommentRepository,
    @Autowired private val userService: UserService,
    @Autowired private val toiletRepository: ToiletRepository
) : CommentService {
    override fun create(createUpdateCommentDto: CreateUpdateCommentDto): Mono<CommentDto> {
        LOG.debug("create: $createUpdateCommentDto")

        return when {
            !ObjectId.isValid(createUpdateCommentDto.toiletId) -> Mono.error(InvalidIdException(createUpdateCommentDto.toiletId))
            else -> {
                val toiletId = ObjectId(createUpdateCommentDto.toiletId)
                val currentUserId = userService.getCurrentUserId()

                toiletRepository
                    .findById(toiletId)
                    .switchIfEmpty(Mono.error(ToiletDoesNotExistException(createUpdateCommentDto.toiletId)))
                    .flatMap { userService.getCurrentUser() }
                    .flatMap {
                        Mono.zip(
                            Mono.just(it),
                            commentRepository.save(
                                Comment(
                                    toiletId = toiletId,
                                    userRef = currentUserId,
                                    date = LocalDateTime.now(),
                                    text = createUpdateCommentDto.text
                                )
                            )
                        )
                    }
                    .map {
                        val userDto = it.t1
                        val comment = it.t2

                        comment.createCommentDto(userDto)
                    }
            }
        }
    }

    private fun getById(id: String): Mono<Comment> {
        LOG.debug("getById: (id=$id)")

        return when {
            !ObjectId.isValid(id) -> Mono.error(InvalidIdException(id))
            else -> commentRepository
                .findById(ObjectId(id))
                .switchIfEmpty(Mono.error(CommentDoesNotExistException(id)))
        }
    }

    override fun update(createUpdateCommentDto: CreateUpdateCommentDto): Mono<CommentDto> {
        LOG.debug("update: $createUpdateCommentDto")

        return getById(createUpdateCommentDto.commentId)
            .flatMap {
                commentRepository.save(
                    it.copy(
                        id = ObjectId(createUpdateCommentDto.commentId),
                        text = createUpdateCommentDto.text,
                        date = LocalDateTime.now()
                    )
                )
            }
            .flatMap { comment ->
                Mono.zip(Mono.just(comment), userService.getById(comment.userRef))
            }
            .map { it.t1.createCommentDto(it.t2) }
    }

    override fun getNumComments(toiletId: ObjectId): Mono<Long> {
        LOG.debug("getNumComments: (toiletId=$toiletId)")

        return commentRepository.getNumComments(toiletId)
    }

    override fun getComments(toiletId: String, page: Int, numComments: Int): Flux<CommentDto> {
        LOG.debug("getComments: (toiletId=$toiletId, page=$page, numComments=$numComments)")

        return when {
            !ObjectId.isValid(toiletId) -> Flux.error(InvalidIdException(toiletId))
            else -> commentRepository
                .findAllByToiletIdOrderByDateDesc(ObjectId(toiletId), PageRequest.of(page, numComments))
                // use flatMapSequential instead of flatMap to keep the sorted order of the comments.
                // Otherwise it will get unsorted again because of the userService.getById call
                .flatMapSequential {
                    userService.getById(it.userRef).zipWith(Mono.just(it))
                }
                .map {
                    val comment = it.t2
                    val userDto = it.t1

                    comment.createCommentDto(userDto)
                }
        }
    }

    @Transactional
    override fun delete(id: String): Mono<Void> {
        LOG.debug("delete: (id=$id)")

        return when {
            !ObjectId.isValid(id) -> Mono.error(InvalidIdException(id))
            else -> commentRepository.deleteById(ObjectId(id))
        }
    }

    override fun deleteByToiletId(toiletId: ObjectId): Mono<Void> {
        LOG.debug("deleteByToiletId: (toiletId=$toiletId)")

        return commentRepository.deleteByToiletId(toiletId)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultCommentService::class.java)
    }
}


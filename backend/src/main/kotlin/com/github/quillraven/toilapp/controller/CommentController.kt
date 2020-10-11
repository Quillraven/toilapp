package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.model.Comment
import com.github.quillraven.toilapp.service.CommentService
import com.github.quillraven.toilapp.service.ToiletService
import com.github.quillraven.toilapp.service.UserService
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.net.URLDecoder

@RestController
@RequestMapping("/api")
class CommentController(
    @Autowired private val commentService: CommentService,
    @Autowired private val userService: UserService,
    @Autowired private val toiletService: ToiletService,
) {
    @PostMapping("/comments")
    fun createComment(
        @RequestParam toiletId: String,
        @RequestParam userId: String,
        @RequestParam text: String
    ): Mono<Comment> {
        LOG.debug("Create comment for toilet |$toiletId| with user |$userId|: |$text|")
        return userService
            // check if user is valid
            .getById(ObjectId(userId))
            // if yes -> create new comment
            .flatMap {
                LOG.debug("Found user to enter comment")
                // TODO how to replace blocking call?
                commentService.create(it, URLDecoder.decode(text, "UTF-8"))
            }
            // if it was created successfully -> link it to toilet
            .zipWith(toiletService.getById(ObjectId(toiletId)))
            .flatMap { tuple ->
                val comment = tuple.t1
                val toilet = tuple.t2

                LOG.debug("Found toilet to enter comment")

                toiletService.update(toilet.id, toilet.apply { comments.add(comment) })
            }
            // and return it
            .map {
                it.comments.last()
            }
    }

    @PutMapping("/comments/{id}")
    fun updateComment(@PathVariable id: String, @RequestParam text: String) = commentService.update(ObjectId(id), text)

    @DeleteMapping("/comments/{id}")
    fun deleteComment(@PathVariable id: String) = commentService.delete(ObjectId(id))

    companion object {
        private val LOG = LoggerFactory.getLogger(CommentController::class.java)
    }
}

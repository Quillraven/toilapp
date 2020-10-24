package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.service.CommentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class CommentController(
    @Autowired private val commentService: CommentService
) {
    @PostMapping("/comments")
    fun createComment(
        @RequestParam toiletId: String,
        @RequestParam userId: String,
        @RequestParam text: String
    ) = commentService.createAndLink(userId, text, toiletId)

    @PutMapping("/comments/{id}")
    fun updateComment(@PathVariable id: String, @RequestParam text: String) = commentService.update(id, text)

    @GetMapping("/comments/{toiletId}")
    fun getComments(@PathVariable toiletId: String) = commentService.getComments(toiletId)

    @DeleteMapping("/comments/{id}")
    fun deleteComment(@PathVariable id: String) = commentService.delete(id)
}

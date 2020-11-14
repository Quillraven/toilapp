package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.model.dto.CreateUpdateCommentDto
import com.github.quillraven.toilapp.service.CommentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class CommentController(
    @Autowired private val commentService: CommentService
) {
    @PutMapping("/comments")
    fun updateComment(@RequestBody createUpdateCommentDto: CreateUpdateCommentDto) =
        commentService.update(createUpdateCommentDto)

    @DeleteMapping("/comments/{id}")
    fun deleteComment(@PathVariable id: String) = commentService.delete(id)
}

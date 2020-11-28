package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.model.dto.CreateUpdateCommentDto
import com.github.quillraven.toilapp.service.CommentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/comments")
class CommentController(
    @Autowired private val commentService: CommentService
) {
    @PostMapping
    fun create(@RequestBody createUpdateCommentDto: CreateUpdateCommentDto) =
        commentService.create(createUpdateCommentDto)

    @PutMapping
    fun update(@RequestBody createUpdateCommentDto: CreateUpdateCommentDto) =
        commentService.update(createUpdateCommentDto)

    @GetMapping("/{toiletId}")
    fun getComments(
        @PathVariable toiletId: String,
        @RequestParam(required = true) page: Int,
        @RequestParam(required = true) numComments: Int
    ) = commentService.getComments(toiletId, page, numComments)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) = commentService.delete(id)
}

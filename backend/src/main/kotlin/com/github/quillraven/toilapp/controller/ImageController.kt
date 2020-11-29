package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.service.ImageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

const val IMAGES_REST_PATH_V1 = "/v1/images"

@RestController
@RequestMapping("/api$IMAGES_REST_PATH_V1")
class ImageController(
    @Autowired private val imageService: ImageService
) {
    @PostMapping("/preview")
    fun createPreviewImage(
        @RequestPart("file") file: Mono<FilePart>,
        @RequestParam("toiletId") toiletId: String
    ) = imageService.createPreview(file, toiletId)

    @PutMapping("/preview")
    fun updatePreviewImage(
        @RequestPart("file") file: Mono<FilePart>,
        @RequestParam("toiletId") toiletId: String
    ) = imageService.updatePreview(file, toiletId)

    @GetMapping(
        value = ["/{id}"],
        produces = [MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE]
    )
    @ResponseBody
    fun getContent(@PathVariable id: String) = imageService.getContent(id)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) = imageService.delete(id)
}

package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.service.IImageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api")
class PreviewImageController(
    @Autowired
    private val imageService: IImageService
) {
    @PostMapping("/previews")
    fun createPreviewImage(@RequestPart("file") file: Mono<FilePart>) = imageService.create(file)

    @GetMapping(
        value = ["/previews/{id}"],
        produces = [MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE]
    )
    @ResponseBody
    fun getPreviewImage(@PathVariable id: String) = imageService.get(id)
}

package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.service.ImageService
import com.github.quillraven.toilapp.service.ToiletService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api")
class PreviewImageController(
    @Autowired private val imageService: ImageService,
    @Autowired private val toiletService: ToiletService
) {
    /*
      FIXME: Transactional is currently not supported by GridFS and therefore we could potentially
       create images that are not linked to any toilet -> let's fix that later ;)
    */
    @PostMapping("/previews")
    @Transactional
    fun createPreviewImage(
        @RequestPart("file") file: Mono<FilePart>,
        @RequestParam("toiletId") toiletId: String
    ) = toiletService.createAndLinkImage(file, toiletId)

    @GetMapping(
        value = ["/previews/{id}"],
        produces = [MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE]
    )
    @ResponseBody
    fun getPreviewImage(@PathVariable id: String) = imageService.get(id)
}

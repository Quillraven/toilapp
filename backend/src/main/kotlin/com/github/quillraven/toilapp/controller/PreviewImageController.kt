package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.PreviewImageDoesNotExistException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

private val LOG = LoggerFactory.getLogger(PreviewImageController::class.java)

@RestController
@RequestMapping("/api")
class PreviewImageController {
    @Autowired
    @Qualifier("reactiveGridFsTemplateForImages")
    lateinit var gridFsTemplate: ReactiveGridFsTemplate

    @PostMapping("/previews")
    fun createPreviewImage(@RequestPart("file") file: Mono<FilePart>): Mono<ResponseEntity<Map<String, String>>> {
        return file
            .flatMap { filePart ->
                LOG.debug(
                    "createPreviewImage: (filename=${filePart.filename()}, contentType=${filePart.headers().contentType})"
                )
                gridFsTemplate.store(
                    filePart.content(),
                    filePart.filename(),
                    filePart.headers().contentType.toString()
                )
            }
            .map { objectId ->
                LOG.debug("Preview image created with id: ${objectId.toHexString()}")
                ok(mapOf("id" to objectId.toHexString()))
            }
    }

    @GetMapping("/previews/{id}")
    @ResponseBody
    fun getPreviewImage(@PathVariable id: String): Mono<ResponseEntity<ByteArray>> {
        LOG.debug("getPreviewImage: $id")
        var fileLength = 0L
        var contentType = MediaType.ALL

        return gridFsTemplate
            // get file
            .findOne(Query.query(Criteria.where("_id").`is`(id)))
            .switchIfEmpty(Mono.error(PreviewImageDoesNotExistException(id)))
            // get content type, file size and resource (=chunks)
            .flatMap { gridFsFile ->
                contentType = MediaType.parseMediaType(gridFsFile.metadata?.getString("_contentType") ?: "")
                fileLength = gridFsFile.length
                LOG.debug("Getting resource of file |${gridFsFile.filename}| with length |${fileLength}| and contentType |$contentType|")
                gridFsTemplate.getResource(gridFsFile)
            }
            // open input stream to retrieve file content
            .flatMap { reactiveGridFsResource ->
                reactiveGridFsResource.inputStream
            }
            // read input stream into byte array
            .map { inputStream ->
                inputStream.use {
                    ok().contentLength(fileLength)
                        .contentType(contentType)
                        .body(it.readBytes())
                }
            }
    }
}

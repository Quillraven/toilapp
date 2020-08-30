package com.github.quillraven.toilapp.controller

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
        return file.flatMap { filePart ->
            LOG.debug(
                "createPreviewImage: (filename=${filePart.filename()}, contentType=${filePart.headers().contentType})"
            )
            gridFsTemplate.store(
                filePart.content(),
                filePart.filename(),
                filePart.headers().contentType.toString()
            )
                .map {
                    LOG.debug("Preview image created with id: ${it.toHexString()}")
                    ok(mapOf("id" to it.toHexString()))
                }
        }
    }

    @GetMapping("/previews/{id}")
    @ResponseBody
    fun getPreviewImage(@PathVariable id: String): Mono<ResponseEntity<ByteArray>> {
        LOG.debug("getPreviewImage: $id")
        return gridFsTemplate.findOne(Query.query(Criteria.where("_id").`is`(id)))
            .flatMap { gridFsFile ->
                val contentType = MediaType.parseMediaType(gridFsFile.metadata?.getString("_contentType") ?: "")
                LOG.debug("Getting resource of file |${gridFsFile.filename}| with length |${gridFsFile.length}|")
                LOG.debug("Resource has type |$contentType|")
                gridFsTemplate.getResource(gridFsFile).flatMap { reactiveGridFsResource ->
                    LOG.debug("Found resource")
                    reactiveGridFsResource.inputStream.map { inputStream ->
                        LOG.debug("Reading file via InputStream")
                        val bytes = ByteArray(gridFsFile.length.toInt())
                        inputStream.run {
                            read(bytes)
                            close()
                        }
                        ok().contentLength(gridFsFile.length)
                            .contentType(contentType)
                            .body(bytes)
                    }
                }
            }
    }
}

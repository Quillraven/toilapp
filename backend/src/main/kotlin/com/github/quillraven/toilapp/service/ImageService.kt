package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.PreviewImageDoesNotExistException
import com.github.quillraven.toilapp.UnsupportedImageContentTypeException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

interface IImageService {
    fun create(filePartMono: Mono<FilePart>): Mono<String>
    fun get(id: String): Mono<ByteArray>
}

private val LOG = LoggerFactory.getLogger(ImageService::class.java)

@Service
class ImageService(
    @Autowired
    @Qualifier("reactiveGridFsTemplateForImages")
    private val gridFsTemplate: ReactiveGridFsTemplate
) : IImageService {
    override fun create(filePartMono: Mono<FilePart>): Mono<String> {
        return filePartMono
            .flatMap { filePart ->
                val fileName = filePart.filename()
                val fileExtension = fileName.substringAfterLast('.', "")
                LOG.debug("create: (fileName=$fileName, extension=$fileExtension)")

                val contentType = when {
                    "jpg".equals(fileExtension, true)
                            || "jpeg".equals(fileExtension, true) -> {
                        MediaType.IMAGE_JPEG_VALUE
                    }
                    "png".equals(fileExtension, true) -> MediaType.IMAGE_PNG_VALUE
                    else -> ""
                }
                LOG.debug("ContentType is $contentType")

                when (contentType) {
                    "" -> Mono.error(UnsupportedImageContentTypeException(fileName))
                    else -> gridFsTemplate.store(
                        filePart.content(),
                        fileName,
                        contentType
                    )
                }
            }
            .map { it.toHexString() }
    }

    override fun get(id: String): Mono<ByteArray> {
        LOG.debug("get: id=$id")
        return gridFsTemplate
            // get file
            .findOne(Query.query(Criteria.where("_id").`is`(id)))
            .switchIfEmpty(Mono.error(PreviewImageDoesNotExistException(id)))
            // get content type, file size and resource (=chunks)
            .flatMap { gridFsFile ->
                gridFsTemplate.getResource(gridFsFile)
            }
            // open input stream to retrieve file content
            .flatMap { reactiveGridFsResource ->
                reactiveGridFsResource.inputStream
            }
            // read input stream into byte array
            .map { inputStream ->
                inputStream.use { it.readBytes() }
            }
    }

}

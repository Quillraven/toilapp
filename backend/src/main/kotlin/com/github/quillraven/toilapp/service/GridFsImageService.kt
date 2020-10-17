package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.PreviewImageDoesNotExistException
import com.github.quillraven.toilapp.UnsupportedImageContentTypeException
import com.github.quillraven.toilapp.model.dto.ToiletDto
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.io.InputStream
import java.util.concurrent.Callable

@Service
class GrisFsImageService(
    @Qualifier("reactiveGridFsTemplateForImages")
    @Autowired private val gridFsTemplate: ReactiveGridFsTemplate
) : ImageService {
    lateinit var toiletService: ToiletService
        @Autowired set

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

    override fun delete(id: String): Mono<Void> {
        LOG.debug("delete: id=$id")
        return gridFsTemplate.delete(Query.query(Criteria.where("_id").`is`(id)))
    }

    override fun store(inStreamCallable: Callable<InputStream>, name: String): Mono<ObjectId> {
        LOG.debug("store: (name=$name)")
        val flux = DataBufferUtils.readInputStream(
            inStreamCallable,
            DefaultDataBufferFactory(),
            DefaultDataBufferFactory.DEFAULT_INITIAL_CAPACITY
        )
        return gridFsTemplate.store(flux, "myName")
    }

    override fun createAndLinkImage(file: Mono<FilePart>, toiletId: String): Mono<ToiletDto> {
        LOG.debug("createAndLinkImage: (toiletId=$toiletId)")
        return toiletService
            .getById(toiletId)
            .zipWith(create(file))
            .flatMap {
                val toilet = it.t1
                val fileId = it.t2
                toiletService.update(toiletId, toilet.copy(previewID = ObjectId(fileId)))
            }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GrisFsImageService::class.java)
    }
}

package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.PreviewImageDoesNotExistException
import com.github.quillraven.toilapp.UnsupportedImageContentTypeException
import com.github.quillraven.toilapp.repository.ToiletRepository
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
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.io.InputStream
import java.util.concurrent.Callable

@Service
class GridFsImageService(
    @Qualifier("reactiveGridFsTemplateForImages")
    @Autowired private val gridFsTemplate: ReactiveGridFsTemplate,
    @Autowired private val toiletRepository: ToiletRepository
) : ImageService {

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
        LOG.debug("get: (id=$id)")
        val imageId = ObjectId(id)

        return gridFsTemplate
            // get file
            .findOne(Query.query(Criteria.where(ID_FIELD_NAME).`is`(imageId)))
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

    @Transactional
    override fun delete(id: String): Mono<Void> {
        LOG.debug("delete: (id=$id)")
        val imageId = ObjectId(id)

        return gridFsTemplate.find(Query.query(Criteria.where(ID_FIELD_NAME).`is`(imageId)))
            // remove preview image from any toilet using this file
            .flatMap {
                toiletRepository.findByPreviewID(imageId)
            }
            .flatMap {
                LOG.debug("Removing image '$imageId' from toilet '${it.id}'")
                toiletRepository.removePreviewID(it.id)
            }
            // finally -> delete the file
            .then(deleteOnlyImage(imageId))
    }

    override fun deleteOnlyImage(imageId: ObjectId): Mono<Void> {
        LOG.debug("Deleting image '$imageId'")
        return gridFsTemplate.delete(Query.query(Criteria.where(ID_FIELD_NAME).`is`(imageId)))
    }

    override fun store(inStreamCallable: Callable<InputStream>, name: String): Mono<ObjectId> {
        LOG.debug("store: (name=$name)")
        val flux = DataBufferUtils.readInputStream(
            inStreamCallable,
            DefaultDataBufferFactory(),
            DefaultDataBufferFactory.DEFAULT_INITIAL_CAPACITY
        )
        return gridFsTemplate.store(flux, name)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GridFsImageService::class.java)
        private const val ID_FIELD_NAME = "_id"
    }
}

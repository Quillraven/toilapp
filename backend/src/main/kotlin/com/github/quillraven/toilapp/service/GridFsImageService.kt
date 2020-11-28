package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.ImageDoesNotExistException
import com.github.quillraven.toilapp.InvalidIdException
import com.github.quillraven.toilapp.ToiletDoesNotExistException
import com.github.quillraven.toilapp.UnsupportedImageContentTypeException
import com.github.quillraven.toilapp.controller.IMAGES_REST_PATH_V1
import com.github.quillraven.toilapp.model.db.IMAGES_COLLECTION_NAME
import com.github.quillraven.toilapp.model.db.Image
import com.github.quillraven.toilapp.model.db.ImageMetadata
import com.github.quillraven.toilapp.repository.ToiletRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class GridFsImageService(
    @Qualifier("reactiveGridFsTemplateForImages")
    @Autowired private val gridFsTemplate: ReactiveGridFsTemplate,
    @Autowired private val mongoTemplate: ReactiveMongoTemplate,
    @Autowired private val toiletRepository: ToiletRepository
) : ImageService {

    override fun createPreview(filePartMono: Mono<FilePart>, toiletId: String): Mono<String> {
        LOG.debug("createPreview: (toiletId=$toiletId)")

        return when {
            !ObjectId.isValid(toiletId) -> Mono.error(InvalidIdException(toiletId))
            else -> {
                val toiletObjectId = ObjectId(toiletId)

                toiletRepository.findById(toiletObjectId)
                    .switchIfEmpty(Mono.error(ToiletDoesNotExistException(toiletId)))
                    .flatMap { filePartMono }
                    .flatMap { filePart ->
                        val fileName = filePart.filename()
                        val fileExtension = fileName.substringAfterLast('.', "")

                        val contentType = when {
                            "jpg".equals(fileExtension, true)
                                    || "jpeg".equals(fileExtension, true) -> {
                                MediaType.IMAGE_JPEG_VALUE
                            }
                            "png".equals(fileExtension, true) -> MediaType.IMAGE_PNG_VALUE
                            else -> ""
                        }

                        LOG.debug("create: (fileName=$fileName, extension=$fileExtension, contentType=$contentType)")
                        when (contentType) {
                            "" -> Mono.error(UnsupportedImageContentTypeException(fileName))
                            else -> gridFsTemplate.store(
                                filePart.content(),
                                fileName,
                                contentType,
                                ImageMetadata(
                                    toiletId = toiletObjectId,
                                    preview = true
                                )
                            )
                        }
                    }
                    .map { it.toHexString() }
            }
        }
    }

    override fun updatePreview(filePartMono: Mono<FilePart>, toiletId: String): Mono<String> {
        LOG.debug("updatePreview: (toiletId=$toiletId)")

        return when {
            !ObjectId.isValid(toiletId) -> Mono.error(InvalidIdException(toiletId))
            else -> {
                val query = Query.query(
                    Criteria.where("$METADATA_FIELD_NAME.${ImageMetadata::toiletId.name}").`is`(ObjectId(toiletId))
                        .and("$METADATA_FIELD_NAME.${ImageMetadata::preview.name}").`is`(true)
                )

                val update = Update()
                    .set("$METADATA_FIELD_NAME.${ImageMetadata::preview.name}", false)

                mongoTemplate
                    // first set the previous preview image to a normal image
                    .findAndModify(query, update, Image::class.java, IMAGES_COLLECTION_NAME)
                    // then create the new preview image
                    .then(createPreview(filePartMono, toiletId))
            }
        }
    }

    override fun getContent(id: String): Mono<ByteArray> {
        LOG.debug("getContent: (id=$id)")

        return when {
            !ObjectId.isValid(id) -> Mono.error(InvalidIdException(id))
            else -> {
                val imageId = ObjectId(id)

                gridFsTemplate
                    // get file
                    .findOne(Query.query(Criteria.where(ID_FIELD_NAME).`is`(imageId)))
                    .switchIfEmpty(Mono.error(ImageDoesNotExistException(id)))
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
    }

    override fun getPreviewURL(toiletId: ObjectId): Mono<String> {
        LOG.debug("getPreviewURL: (toiletId=$toiletId)")

        return gridFsTemplate
            .findOne(
                Query.query(
                    Criteria.where("$METADATA_FIELD_NAME.${ImageMetadata::toiletId.name}").`is`(toiletId)
                        .and("$METADATA_FIELD_NAME.${ImageMetadata::preview.name}").`is`(true)
                )
            )
            .map {
                "$IMAGES_REST_PATH_V1/${it.id.asObjectId().value.toHexString()}"
            }
            .switchIfEmpty(Mono.just(""))
    }

    override fun delete(id: String): Mono<Void> {
        LOG.debug("delete: (id=$id)")

        return when {
            !ObjectId.isValid(id) -> Mono.error(InvalidIdException(id))
            else -> gridFsTemplate.delete(Query.query(Criteria.where(ID_FIELD_NAME).`is`(ObjectId(id))))
        }
    }

    override fun deleteByToiletId(toiletId: ObjectId): Mono<Void> {
        LOG.debug("deleteByToiletId: (toiletId=$toiletId)")

        return gridFsTemplate.delete(
            Query.query(
                Criteria.where("$METADATA_FIELD_NAME.${ImageMetadata::toiletId.name}").`is`(toiletId)
            )
        )
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GridFsImageService::class.java)
        private const val ID_FIELD_NAME = "_id"
        private const val METADATA_FIELD_NAME = "metadata"
    }
}

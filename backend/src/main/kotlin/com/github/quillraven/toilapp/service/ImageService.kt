package com.github.quillraven.toilapp.service

import org.bson.types.ObjectId
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Mono

interface ImageService {
    fun createPreview(filePartMono: Mono<FilePart>, toiletId: String): Mono<String>
    fun updatePreview(filePartMono: Mono<FilePart>, toiletId: String): Mono<String>
    fun getContent(id: String): Mono<ByteArray>
    fun getPreviewURL(toiletId: ObjectId): Mono<String>
    fun delete(id: String): Mono<Void>
    fun deleteByToiletId(toiletId: ObjectId): Mono<Void>
}

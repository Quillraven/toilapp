package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.model.dto.ToiletDto
import org.bson.types.ObjectId
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Mono
import java.io.InputStream
import java.util.concurrent.Callable

interface ImageService {
    fun create(filePartMono: Mono<FilePart>): Mono<String>
    fun get(id: String): Mono<ByteArray>
    fun store(inStreamCallable: Callable<InputStream>, name: String): Mono<ObjectId>
    fun createAndLinkImage(file: Mono<FilePart>, toiletId: String): Mono<ToiletDto>
}

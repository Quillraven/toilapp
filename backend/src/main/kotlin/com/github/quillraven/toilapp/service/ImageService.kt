package com.github.quillraven.toilapp.service

import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Mono

interface ImageService {
    fun create(filePartMono: Mono<FilePart>): Mono<String>
    fun get(id: String): Mono<ByteArray>
}
package com.github.quillraven.toilapp.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.quillraven.toilapp.model.ModelWithFields
import com.github.quillraven.toilapp.model.ModelWithNoFields
import com.github.quillraven.toilapp.model.db.Toilet
import com.github.quillraven.toilapp.model.dto.ToiletDto
import com.github.quillraven.toilapp.repository.CommentRepository
import com.github.quillraven.toilapp.repository.ToiletRepository
import com.github.quillraven.toilapp.service.DefaultCommentService
import com.github.quillraven.toilapp.service.DefaultToiletService
import com.github.quillraven.toilapp.service.GridFsImageService
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.`should be equal to`
import org.bson.types.ObjectId
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.data.mongodb.core.geo.GeoJsonModule
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

@WebFluxTest(controllers = [ToiletController::class])
object ToiletControllerSpec : Spek({
    val toiletRepository: ToiletRepository by memoized { mockk<ToiletRepository>() }
    val commentRepository: CommentRepository by memoized { mockk<CommentRepository>() }
    val gridFsTemplate: ReactiveGridFsTemplate by memoized { mockk<ReactiveGridFsTemplate>() }
    val commentService: DefaultCommentService by memoized { DefaultCommentService(commentRepository) }
    val imageService: GridFsImageService by memoized { GridFsImageService(gridFsTemplate) }
    val toiletService: DefaultToiletService by memoized {
        DefaultToiletService(
            toiletRepository,
            commentService,
            imageService
        )
    }
    val client by memoized {
        WebTestClient
            .bindToController(ToiletController(toiletService))
            .build()
            .mutateWith { builder, _, _ ->
                builder.codecs {
                    val objectMapper = jacksonObjectMapper().apply {
                        registerModule(GeoJsonModule())
                    }
                    it.defaultCodecs().jackson2JsonEncoder(
                        Jackson2JsonEncoder(objectMapper)
                    )
                    it.defaultCodecs().jackson2JsonDecoder(
                        Jackson2JsonDecoder(objectMapper)
                    )
                }
            }
    }

    describe("A ToiletController") {
        describe("Retrieving non-null fields of a class with zero null non-null fields") {
            it("should return an empty array") {
                ModelWithNoFields()::class.getNonNullProperties() `should be equal to` arrayOf()
            }
        }

        describe("Retrieving non-null fields of a class with a non-null field") {
            it("should return an array containing the name of the field") {
                val expected = ModelWithFields()::class.getNonNullProperties()

                expected `should be equal to` arrayOf(ModelWithFields::field1.name, ModelWithFields::field2.name)
            }
        }

        describe("Retrieving non-null fields of a class with a non-null field using exceptions") {
            it("should return an array containing the name of the field") {
                val expected = ModelWithFields()::class.getNonNullProperties(ModelWithFields::field2)

                expected `should be equal to` arrayOf("field1")
            }
        }

        it("should return flux with two toilets") {
            val toilet1 = Toilet(id = ObjectId())
            val toilet2 = Toilet(id = ObjectId())
            every { toiletRepository.findAll() } returns Flux.just(
                toilet1,
                toilet2
            )

            val result = client.get().uri("/api/toilets")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk
                .returnResult<ToiletDto>()
                .responseBody

            StepVerifier.create(result)
                .expectNext(toiletService.createToiletDto(toilet1))
                .expectNext(toiletService.createToiletDto(toilet2))
                .expectComplete()
                .verify()
        }
    }
})

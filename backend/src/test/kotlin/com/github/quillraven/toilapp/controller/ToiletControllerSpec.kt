package com.github.quillraven.toilapp.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.quillraven.toilapp.model.db.ToiletDistanceInfo
import com.github.quillraven.toilapp.model.dto.ToiletOverviewDto
import com.github.quillraven.toilapp.repository.ToiletRepository
import com.github.quillraven.toilapp.service.CommentService
import com.github.quillraven.toilapp.service.DefaultToiletService
import com.github.quillraven.toilapp.service.ImageService
import com.github.quillraven.toilapp.service.RatingService
import com.github.quillraven.toilapp.service.ToiletService
import io.mockk.every
import io.mockk.mockk
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.data.mongodb.core.geo.GeoJsonModule
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@WebFluxTest(controllers = [ToiletController::class])
object ToiletControllerSpec : Spek({
    val toiletRepository: ToiletRepository by memoized { mockk<ToiletRepository>() }
    val imageService: ImageService by memoized { mockk<ImageService>() }
    val commentService: CommentService by memoized { mockk<CommentService>() }
    val ratingService: RatingService by memoized { mockk<RatingService>() }
    val toiletService: ToiletService by memoized {
        DefaultToiletService(
            toiletRepository, imageService, commentService, ratingService
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

    describe("ToiletController") {
        it("should return flux with three toilets and different distances") {
            val lon = 0.0
            val lat = 0.0
            val maxDistanceInKm = 400.0
            val maxToiletsToLoad = 20L
            val expectedResult1 = ToiletDistanceInfo(distance = 10.0)
            val expectedResult2 = ToiletDistanceInfo(distance = 20.0)
            val expectedResult3 = ToiletDistanceInfo(distance = 30.0)
            every {
                toiletRepository.getNearbyToilets(GeoJsonPoint(lon, lat), maxDistanceInKm, maxToiletsToLoad)
            } returns Flux.just(
                expectedResult1,
                expectedResult2,
                expectedResult3
            )
            every { ratingService.getAverageRating(any()) } returns Mono.just(0.0)
            every { imageService.getPreviewURL(any()) } returns Mono.just("")

            val result = client.get().uri(
                "/api/v1/toilets?" +
                        "lon=$lon&" +
                        "lat=$lat&" +
                        "radiusInKm=$maxDistanceInKm&" +
                        "maxToiletsToLoad=$maxToiletsToLoad"
            )
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk
                .returnResult<ToiletOverviewDto>()
                .responseBody

            StepVerifier.create(result)
                .expectNext(expectedResult1.createToiletOverviewDto("", 0.0))
                .expectNext(expectedResult2.createToiletOverviewDto("", 0.0))
                .expectNext(expectedResult3.createToiletOverviewDto("", 0.0))
                .expectComplete()
                .verify()
        }
    }
})

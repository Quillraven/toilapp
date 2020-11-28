package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.InvalidIdException
import com.github.quillraven.toilapp.model.dto.CreateUpdateToiletDto
import com.github.quillraven.toilapp.repository.ToiletRepository
import io.mockk.mockk
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import reactor.test.StepVerifier

object ToiletServiceSpec : Spek({
    val toiletRepository: ToiletRepository by memoized { mockk<ToiletRepository>() }
    val imageService: ImageService by memoized { mockk<ImageService>() }
    val commentService: CommentService by memoized { mockk<CommentService>() }
    val ratingService: RatingService by memoized { mockk<RatingService>() }
    val toiletService: ToiletService by memoized {
        DefaultToiletService(
            toiletRepository, imageService, commentService, ratingService
        )
    }

    describe("ToiletService") {
        it("should throw InvalidIdException") {
            val createUpdateToiletDto = CreateUpdateToiletDto(
                id = "invalidID",
                title = "",
                description = "",
                location = GeoJsonPoint(0.0, 0.0)
            )

            StepVerifier
                .create(toiletService.update(createUpdateToiletDto))
                .expectErrorMatches { throwable ->
                    throwable is InvalidIdException
                            && throwable.message == "422 Id '${createUpdateToiletDto.id}' is not a correct hex id with 24 characters"

                }
                .verify()
        }
    }
})

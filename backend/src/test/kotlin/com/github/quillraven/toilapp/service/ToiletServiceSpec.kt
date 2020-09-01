package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.ToiletDoesNotExistException
import com.github.quillraven.toilapp.model.Toilet
import com.github.quillraven.toilapp.repository.ToiletRepository
import io.mockk.every
import io.mockk.mockk
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

object ToiletServiceSpec : Spek({
    val toiletRepository: ToiletRepository by memoized { mockk() }
    val toiletService: IToiletService by memoized { ToiletService(toiletRepository) }

    describe("A Toilet service") {
        it("should create a new toilet with id 1") {
            val expectedToilet = Toilet(id = "1")
            every { toiletRepository.save(any()) } returns Mono.just(expectedToilet)

            StepVerifier
                .create(toiletService.create(expectedToilet))
                .expectNext(expectedToilet)
                .expectComplete()
                .verify()
        }

        it("should update toilet description from A to B") {
            val existingToilet = Toilet(id = "1", description = "A")
            val expectedToilet = Toilet(id = "1", description = "B")
            every { toiletRepository.findById("1") } returns Mono.just(existingToilet)
            every { toiletRepository.save(any()) } returns Mono.just(expectedToilet)

            StepVerifier
                .create(toiletService.update("1", expectedToilet))
                .expectNext(expectedToilet)
                .expectComplete()
                .verify()
        }

        it("should throw ToiletDoesNotExistException") {
            every { toiletRepository.findById("1") } returns Mono.empty()

            StepVerifier
                .create(toiletService.update("1", Toilet()))
                .expectErrorMatches {
                    it is ToiletDoesNotExistException
                            && it.message == "404 Toilet of id |1| does not exist!"
                }
                .verify()
        }
    }
})
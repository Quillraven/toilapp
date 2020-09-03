package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.ToiletDoesNotExistException
import com.github.quillraven.toilapp.model.Toilet
import com.github.quillraven.toilapp.repository.ToiletRepository
import io.mockk.every
import io.mockk.mockk
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import reactor.core.publisher.Flux
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

        it("should return two toilets") {
            val toilet1 = Toilet(id = "1")
            val toilet2 = Toilet(id = "2")
            every { toiletRepository.findAll() } returns Flux.just(toilet1, toilet2)

            StepVerifier
                .create(toiletService.getAll())
                .expectNext(toilet1)
                .expectNext(toilet2)
                .expectComplete()
                .verify()
        }

        it("should return empty Flux") {
            every { toiletRepository.findAll() } returns Flux.empty()

            StepVerifier
                .create(toiletService.getAll())
                .expectComplete()
                .verify()
        }

        it("should return Mono<Void>") {
            every { toiletRepository.findById(any<String>()) } returns Mono.just(Toilet(id = "1"))
            every { toiletRepository.deleteById(any<String>()) } returns Mono.empty()

            StepVerifier
                .create(toiletService.delete("1"))
                .expectComplete()
                .verify()
        }

        it("should throw ToiletDoesNotExistException") {
            every { toiletRepository.findById(any<String>()) } returns Mono.empty()
            every { toiletRepository.deleteById(any<String>()) } returns Mono.empty()

            StepVerifier
                .create(toiletService.delete("1"))
                .expectErrorMatches {
                    it is ToiletDoesNotExistException
                            && it.message == "404 Toilet of id |1| does not exist!"
                }
                .verify()
        }
    }
})
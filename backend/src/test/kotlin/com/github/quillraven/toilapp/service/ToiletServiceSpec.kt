package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.ToiletDoesNotExistException
import com.github.quillraven.toilapp.model.db.Toilet
import com.github.quillraven.toilapp.repository.ToiletRepository
import io.mockk.every
import io.mockk.mockk
import org.bson.types.ObjectId
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

object ToiletServiceSpec : Spek({
    val toiletRepository: ToiletRepository by memoized { mockk<ToiletRepository>() }
    val toiletService: DefaultToiletService by memoized { DefaultToiletService(toiletRepository) }

    describe("A Toilet service") {
        it("should create a new toilet") {
            val expectedToilet = Toilet()
            every { toiletRepository.save<Toilet>(any()) } returns Mono.just(expectedToilet)

            StepVerifier
                .create(toiletService.create(expectedToilet))
                .expectNext(toiletService.createToiletDto(expectedToilet))
                .expectComplete()
                .verify()
        }

        it("should update toilet description from A to B") {
            val id = ObjectId()
            val existingToilet = Toilet(id = id, description = "A")
            val expectedToilet = Toilet(id = id, description = "B")
            every { toiletRepository.findById(id) } returns Mono.just(existingToilet)
            every { toiletRepository.save<Toilet>(any()) } returns Mono.just(expectedToilet)

            StepVerifier
                .create(toiletService.update(id.toHexString(), expectedToilet))
                .expectNext(toiletService.createToiletDto(expectedToilet))
                .expectComplete()
                .verify()
        }

        it("should throw ToiletDoesNotExistException") {
            val id = ObjectId()
            every { toiletRepository.findById(id) } returns Mono.empty()

            StepVerifier
                .create(toiletService.update(id.toHexString(), Toilet()))
                .expectErrorMatches {
                    it is ToiletDoesNotExistException
                            && it.message == "404 Toilet of id '$id' does not exist!"
                }
                .verify()
        }

        it("should return two toilets") {
            val toilet1 = Toilet()
            val toilet2 = Toilet()
            every { toiletRepository.findAll() } returns Flux.just(toilet1, toilet2)

            StepVerifier
                .create(toiletService.getAll())
                .expectNext(toiletService.createToiletDto(toilet1))
                .expectNext(toiletService.createToiletDto(toilet2))
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
            val id = ObjectId()
            every { toiletRepository.findById(id) } returns Mono.just(Toilet())
            every { toiletRepository.deleteById(id) } returns Mono.empty()

            StepVerifier
                .create(toiletService.delete(id.toHexString()))
                .expectComplete()
                .verify()
        }

        it("should return Mono<Void>") {
            val id = ObjectId()
            every { toiletRepository.findById(id) } returns Mono.empty()
            every { toiletRepository.deleteById(id) } returns Mono.empty()

            StepVerifier
                .create(toiletService.delete(id.toHexString()))
                .expectComplete()
                .verify()
        }
    }
})

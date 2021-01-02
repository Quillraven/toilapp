package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.InvalidIdException
import com.github.quillraven.toilapp.InvalidRatingValueException
import com.github.quillraven.toilapp.RatingDoesNotExistException
import com.github.quillraven.toilapp.ToiletDoesNotExistException
import com.github.quillraven.toilapp.model.db.Rating
import com.github.quillraven.toilapp.model.db.Toilet
import com.github.quillraven.toilapp.model.dto.CreateUpdateRatingDto
import com.github.quillraven.toilapp.model.dto.RatingDto
import com.github.quillraven.toilapp.repository.RatingRepository
import com.github.quillraven.toilapp.repository.ToiletRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

interface RatingService {
    fun create(createUpdateRatingDto: CreateUpdateRatingDto): Mono<RatingDto>
    fun update(createUpdateRatingDto: CreateUpdateRatingDto): Mono<RatingDto>
    fun getAverageRating(toilet: Toilet): Mono<Double>
    fun getUserRating(toiletId: String): Mono<RatingDto>
    fun delete(id: String): Mono<Void>
    fun deleteByToiletId(toiletId: ObjectId): Mono<Void>
}

@Service
class DefaultRatingService(
    @Autowired private val ratingRepository: RatingRepository,
    @Autowired private val userService: UserService,
    @Autowired private val toiletRepository: ToiletRepository
) : RatingService {
    override fun create(createUpdateRatingDto: CreateUpdateRatingDto): Mono<RatingDto> {
        LOG.debug("create: $createUpdateRatingDto")

        return when {
            !ObjectId.isValid(createUpdateRatingDto.toiletId) -> Mono.error(InvalidIdException(createUpdateRatingDto.toiletId))
            !createUpdateRatingDto.isValidValue() -> Mono.error(InvalidRatingValueException(createUpdateRatingDto.value))
            else -> {
                val toiletId = ObjectId(createUpdateRatingDto.toiletId)
                val currentUserId = userService.getCurrentUserId()

                toiletRepository
                    .findById(toiletId)
                    .switchIfEmpty(Mono.error(ToiletDoesNotExistException(createUpdateRatingDto.toiletId)))
                    .flatMap { userService.getCurrentUser() }
                    .flatMap {
                        Mono.zip(
                            Mono.just(it),
                            ratingRepository.save(
                                Rating(
                                    toiletId = toiletId,
                                    userRef = currentUserId,
                                    value = createUpdateRatingDto.value
                                )
                            )
                        )
                    }
                    .map {
                        val userDto = it.t1
                        val rating = it.t2
                        rating.createRatingDto(userDto)
                    }
            }
        }
    }

    private fun getById(id: String): Mono<Rating> {
        LOG.debug("getById: (id=$id)")

        return when {
            !ObjectId.isValid(id) -> Mono.error(InvalidIdException(id))
            else -> ratingRepository
                .findById(ObjectId(id))
                .switchIfEmpty(Mono.error(RatingDoesNotExistException(id)))
        }
    }

    override fun update(createUpdateRatingDto: CreateUpdateRatingDto): Mono<RatingDto> {
        LOG.debug("update: $createUpdateRatingDto")

        return when {
            !createUpdateRatingDto.isValidValue() -> Mono.error(InvalidRatingValueException(createUpdateRatingDto.value))
            else -> getById(createUpdateRatingDto.ratingId)
                .flatMap {
                    ratingRepository.save(
                        it.copy(
                            id = ObjectId(createUpdateRatingDto.ratingId),
                            value = createUpdateRatingDto.value
                        )
                    )
                }
                .flatMap { rating ->
                    Mono.zip(Mono.just(rating), userService.getById(rating.userRef))
                }
                .map {
                    it.t1.createRatingDto(it.t2)
                }
        }
    }

    override fun getAverageRating(toilet: Toilet): Mono<Double> {
        LOG.debug("getAverageRating: (toilet=$toilet)")

        return ratingRepository.getAverageRating(toilet)
    }

    override fun getUserRating(toiletId: String): Mono<RatingDto> {
        LOG.debug("getUserRating: (toiletId=$toiletId)")

        return when {
            !ObjectId.isValid(toiletId) -> Mono.error(InvalidIdException(toiletId))
            else -> {
                userService.getCurrentUser()
                    .flatMap { userDto ->
                        Mono.zip(
                            Mono.just(userDto),
                            ratingRepository.getByToiletIdAndUserRef(ObjectId(toiletId), ObjectId(userDto.id))
                        )
                    }
                    .map { tuple ->
                        tuple.t2.createRatingDto(tuple.t1)
                    }
            }
        }
    }

    override fun delete(id: String): Mono<Void> {
        LOG.debug("delete: (id=$id)")

        return when {
            !ObjectId.isValid(id) -> Mono.error(InvalidIdException(id))
            else -> ratingRepository.deleteById(ObjectId(id))
        }
    }

    override fun deleteByToiletId(toiletId: ObjectId): Mono<Void> {
        LOG.debug("deleteByToiletId: (toiletId=$toiletId)")

        return ratingRepository.deleteByToiletId(toiletId)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultRatingService::class.java)
    }
}

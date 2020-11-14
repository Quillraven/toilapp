package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.RatingDoesNotExistException
import com.github.quillraven.toilapp.model.db.Rating
import com.github.quillraven.toilapp.model.db.User
import com.github.quillraven.toilapp.model.dto.CreateUpdateRatingDto
import com.github.quillraven.toilapp.model.dto.RatingDto
import com.github.quillraven.toilapp.model.dto.UserDto
import com.github.quillraven.toilapp.repository.RatingRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

interface RatingService {
    fun create(userId: ObjectId, value: Double): Mono<RatingDto>
    fun update(createUpdateRatingDto: CreateUpdateRatingDto): Mono<RatingDto>
    fun getById(id: String): Mono<Rating>
    fun delete(id: String): Mono<Void>
}

@Service
class DefaultRatingService(
    @Autowired private val ratingRepository: RatingRepository
) : RatingService {

    /**
     * Returns a [RatingDto] instance out of the given [rating].
     * The [UserDto] of the comment only contains the id. Name and email
     * need to be fetched separately.
     */
    private fun createRatingDto(rating: Rating, user: User? = null) = RatingDto(
        rating.id.toHexString(),
        UserDto(rating.userRef.toHexString(), user?.name ?: "", ""),
        rating.value
    )

    override fun create(userId: ObjectId, value: Double): Mono<RatingDto> {
        LOG.debug("create: (userId=$userId, value=$value)")
        return ratingRepository
            .save(Rating(userRef = userId, value = value))
            .map { createRatingDto(it) }
    }

    override fun update(createUpdateRatingDto: CreateUpdateRatingDto): Mono<RatingDto> {
        LOG.debug("update: $createUpdateRatingDto")
        return getById(createUpdateRatingDto.ratingId)
            .flatMap {
                ratingRepository.save(
                    it.copy(
                        id = ObjectId(createUpdateRatingDto.ratingId),
                        value = createUpdateRatingDto.value
                    )
                )
            }
            .map { createRatingDto(it) }
    }

    override fun getById(id: String): Mono<Rating> {
        LOG.debug("getById: (id=$id)")
        return ratingRepository
            .findById(ObjectId(id))
            .switchIfEmpty(Mono.error(RatingDoesNotExistException(id)))
    }

    override fun delete(id: String): Mono<Void> {
        LOG.debug("delete: (id=$id)")
        return ratingRepository.findById(ObjectId(id))
            .flatMap { ratingRepository.deleteById(ObjectId(id)) }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultRatingService::class.java)
    }
}

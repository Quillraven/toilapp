package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.RatingDoesNotExistException
import com.github.quillraven.toilapp.model.db.Rating
import com.github.quillraven.toilapp.model.db.User
import com.github.quillraven.toilapp.model.dto.CreateUpdateRatingDto
import com.github.quillraven.toilapp.model.dto.RatingDto
import com.github.quillraven.toilapp.model.dto.UserDto
import com.github.quillraven.toilapp.repository.RatingRepository
import com.github.quillraven.toilapp.repository.ToiletRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.util.function.Tuples

interface RatingService {
    fun createRatingDto(rating: Rating, user: User? = null): RatingDto
    fun create(userId: ObjectId, value: Int): Mono<Rating>
    fun getById(id: String): Mono<Rating>
    fun update(createUpdateRatingDto: CreateUpdateRatingDto): Mono<Rating>
    fun delete(id: String): Mono<Void>
    fun deleteOnlyRating(ratingId: ObjectId): Mono<Void>
}

@Service
class DefaultRatingService(
    @Autowired private val ratingRepository: RatingRepository,
    @Autowired private val toiletRepository: ToiletRepository
) : RatingService {

    /**
     * Returns a [RatingDto] instance out of the given [rating].
     * The [UserDto] of the comment only contains the id. Name and email
     * need to be fetched separately.
     */
    override fun createRatingDto(rating: Rating, user: User?) = RatingDto(
        rating.id.toHexString(),
        UserDto(rating.userRef.toHexString(), user?.name ?: "", ""),
        rating.value
    )

    override fun create(userId: ObjectId, value: Int): Mono<Rating> {
        LOG.debug("create: (userId=$userId, value=$value)")
        return ratingRepository
            .save(Rating(userRef = userId, value = value))
    }

    override fun getById(id: String): Mono<Rating> {
        LOG.debug("getById: (id=$id)")
        return ratingRepository
            .findById(ObjectId(id))
            .switchIfEmpty(Mono.error(RatingDoesNotExistException(id)))
    }

    override fun update(createUpdateRatingDto: CreateUpdateRatingDto): Mono<Rating> {
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
    }

    @Transactional
    override fun delete(id: String): Mono<Void> {
        LOG.debug("delete: (id=$id)")
        val ratingId = ObjectId(id)

        return ratingRepository.findById(ratingId)
            .flatMapMany { rating ->
                toiletRepository.findByRatingRefsContains(ratingId).map {
                    Tuples.of(rating, it)
                }
            }
            // remove rating from any toilets
            .flatMap {
                val rating = it.t1
                val toilet = it.t2

                LOG.debug("Deleting rating from toilet ${toilet.id}")

                toiletRepository.removeRating(toilet.id, rating.id, rating.value)
            }
            // remove rating itself
            .then(deleteOnlyRating(ratingId))
    }

    override fun deleteOnlyRating(ratingId: ObjectId): Mono<Void> {
        LOG.debug("Deleting rating '$ratingId'")
        return ratingRepository.deleteById(ratingId)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultRatingService::class.java)
    }
}

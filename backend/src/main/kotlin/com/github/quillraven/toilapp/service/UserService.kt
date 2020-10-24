package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.UserDoesNotExistException
import com.github.quillraven.toilapp.model.db.User
import com.github.quillraven.toilapp.model.dto.UserDto
import com.github.quillraven.toilapp.repository.UserRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

interface UserService {
    fun create(user: User): Mono<UserDto>
    fun getById(id: String): Mono<User>
}

@Service
class DefaultUserService(
    @Autowired private val userRepository: UserRepository
) : UserService {
    /**
     * Returns a [UserDto] instance out of the given [user].
     */
    private fun createUserDto(user: User) = UserDto(
        user.id.toHexString(),
        user.name,
        user.email
    )

    override fun create(user: User): Mono<UserDto> {
        LOG.debug("create: (user=$user)")
        return userRepository
            .save(user)
            .map { createUserDto(it) }
    }

    override fun getById(id: String): Mono<User> {
        LOG.debug("getById: (id=$id)")
        return userRepository
            .findById(ObjectId(id))
            .switchIfEmpty(Mono.error(UserDoesNotExistException(id)))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultUserService::class.java)
    }
}

package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.ToilappSystemProperties
import com.github.quillraven.toilapp.UserDoesNotExistException
import com.github.quillraven.toilapp.model.db.User
import com.github.quillraven.toilapp.model.dto.CreateUpdateUserDto
import com.github.quillraven.toilapp.model.dto.UserDto
import com.github.quillraven.toilapp.repository.UserRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

interface UserService {
    fun create(createUpdateUserDto: CreateUpdateUserDto): Mono<UserDto>
    fun getCurrentUserId(): ObjectId
    fun getCurrentUser(): Mono<UserDto>
    fun getById(userId: ObjectId): Mono<UserDto>
}

@Service
class DefaultUserService(
    @Autowired private val userRepository: UserRepository
) : UserService {
    override fun create(createUpdateUserDto: CreateUpdateUserDto): Mono<UserDto> {
        LOG.debug("create: $createUpdateUserDto")

        return userRepository
            .save(
                User(
                    email = createUpdateUserDto.email,
                    name = createUpdateUserDto.name
                )
            )
            .map { it.createUserDto() }
    }

    override fun getCurrentUserId(): ObjectId {
        return when {
            ToilappSystemProperties.isDevMode() -> {
                //return dev user created by DataLoaderApplication
                ObjectId("5fc2600fa23d8d7fcaba9e94")
            }
            else -> {
                //TODO return user from request
                ObjectId("5fc2600fa23d8d7fcaba9e94")
            }
        }
    }

    override fun getCurrentUser(): Mono<UserDto> {
        LOG.debug("getCurrentUser")

        return getById(getCurrentUserId())
    }

    override fun getById(userId: ObjectId): Mono<UserDto> {
        LOG.debug("getById: (userId=$userId)")

        return userRepository
            .findById(userId)
            .switchIfEmpty(Mono.error(UserDoesNotExistException(userId.toHexString())))
            .map { it.createUserDto() }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DefaultUserService::class.java)
    }
}

package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.UserDoesNotExistException
import com.github.quillraven.toilapp.model.User
import com.github.quillraven.toilapp.repository.UserRepository
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

interface UserService {
    fun create(user: User): Mono<User>
    fun getById(id: ObjectId): Mono<User>
}

@Service
class DefaultUserService(
    @Autowired private val userRepository: UserRepository
) : UserService {

    override fun create(user: User): Mono<User> {
        return userRepository.save(user)
    }

    override fun getById(id: ObjectId): Mono<User> {
        return userRepository
            .findById(id.toHexString())
            .switchIfEmpty(Mono.error(UserDoesNotExistException(id.toHexString())))
    }
}

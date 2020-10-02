package com.github.quillraven.toilapp.service

import com.github.quillraven.toilapp.model.User
import com.github.quillraven.toilapp.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

interface UserService {
    fun createUser(user: User): Mono<User>
}

@Service
class DefaultUserService(
        @Autowired private val userRepository: UserRepository
) : UserService {

    override fun createUser(user: User): Mono<User> {
        return userRepository.save(user)
    }
}
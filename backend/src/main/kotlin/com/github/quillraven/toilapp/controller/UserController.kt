package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.model.db.User
import com.github.quillraven.toilapp.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class UserController(
    @Autowired private val userService: UserService,
) {
    @PostMapping("/users")
    fun createUser(@RequestBody user: User) = userService.create(user)
}

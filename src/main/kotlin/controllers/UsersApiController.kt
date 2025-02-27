package controllers

import jakarta.validation.Valid
import models.User
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import services.UserService

class UsersApiController(private val userService: UserService) {

    @PostMapping("/api/users")
    fun createUser(@Valid @RequestBody usersDto) :  ResponseEntity<User> {
        val user = userService.createUser(usersDto)
    }
}
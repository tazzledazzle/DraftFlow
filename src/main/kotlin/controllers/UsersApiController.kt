package controllers

import dto.UsersCreateDto
import dto.UsersDto
import jakarta.validation.Valid
import models.User
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import services.UserService


/*
    * Endpoint	Method	Description	Request	Response
        /api/users	POST	Create User	UsersDto	UsersDto

        /api/users/{id}	GET	Get a specific user	-	UsersDto

        /api/users/{id}	PUT	update a user	UsersDto	UsersDto
        /api/users/{id}	DELETE	Delete User	- 	204 No Content
*
*
*
* */

class UsersApiController(private val userService: UserService) {

    @PostMapping("/api/users")
    fun createUser(@Valid @RequestBody usersDto: UsersCreateDto) :  ResponseEntity<User> {
        val user = userService.createUser(usersDto)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/api/users/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<User> {
        return userService.getUserById(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @PutMapping("/api/users/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody usersDto: UsersDto
    ): ResponseEntity<User> {
        return userService.updateUser(id, usersDto)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/api/users/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Unit> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }
}
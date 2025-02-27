package services

import dto.UsersCreateDto
import dto.UsersDto
import dto.UsersUpdateDto
import jakarta.persistence.Id
import models.User
import models.UserRole
import org.springframework.web.bind.annotation.PathVariable
import java.time.LocalDate
import java.time.LocalDateTime

class UserService {
    fun createUser(
        firstName: String = "",
        lastName: String = "",
        email: String = "",
        password: String = "",
        userRole: UserRole = UserRole.FOREMAN,
        createdAt: LocalDateTime = LocalDateTime.now()
    ): User {
        println("User created: $firstName")

        return User(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password,
            role = userRole,
            createdAt = createdAt
        )
    }

    fun createUser(userDto: UsersCreateDto) : User {
        return createUser(
            userDto.firstName,
            userDto.lastName,
            userDto.email,
            userDto.password,
            userDto.role,
            userDto.createdAt
        )
    }

    fun getUserById(string: Long): User? {
        TODO("Not yet implemented")
    }

    fun updateUser(id: Long,
                   userUpdateDto: UsersUpdateDto): User? {
        val user = getUserById(id) ?: return null

    }
}
package services

import models.User
import models.UserRole
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

    fun createUser(userDto: UserCreateDto) : User {

    }
}
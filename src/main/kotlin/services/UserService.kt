package services

import dto.UsersCreateDto
import dto.UsersUpdateDto
import models.User
import models.UserRole
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

    fun getUserById(id: Long): User? {
        return getUserById(id)
    }

    fun updateUser(id: Long,
                   userUpdateDto: UsersUpdateDto): User? {
        val user = getUserById(id) ?: return null
        return null
    }

    fun deleteUser(id: Long): Boolean {
        TODO("Not yet implemented")
    }
}
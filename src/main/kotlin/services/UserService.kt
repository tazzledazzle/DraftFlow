package services

import models.User
import models.UserRole

class UserService {
    fun createUser(firstName: String, lastName: String = "", email: String = "", password: String = "", userRole: UserRole = UserRole.FOREMAN, createdAt: String = ""): User {
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
}
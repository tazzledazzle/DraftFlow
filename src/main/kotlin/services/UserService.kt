package services

import models.User

class UserService {
    fun createUser(name: String): User {
        println("User created: $name")

        return User(firstName = name)
    }
}
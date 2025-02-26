package services

import io.kotest.core.spec.style.FunSpec

class UserServiceTest: FunSpec({
    test("can create user") {
        val userService = UserService()
        userService.createUser(firstName = "John", lastName = "Doe")
    }
})
import io.kotest.core.spec.style.FunSpec
import services.UserService

class UserServiceTest: FunSpec({
    test("can create user") {
        val userService = UserService()
        userService.createUser(firstName = "John", lastName = "Doe")
    }
})
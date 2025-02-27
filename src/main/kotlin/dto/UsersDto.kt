package dto

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.validation.constraints.NotBlank
import models.UserRole
import java.time.LocalDateTime

data class UsersDto (
    var id: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var password: String = "",
    var role: UserRole = UserRole.FOREMAN, // foreman role only pushes data, more secure
    var createdAt: LocalDateTime = LocalDateTime.now()
)
data class UsersCreateDto (
    var firstName: String = "",
    var lastName: String = "",
    @field:NotBlank(message = "Email is required")
    var email: String = "",
    @field:NotBlank(message = "Password is required")
    var password: String = "",
    var role: UserRole = UserRole.FOREMAN, // foreman role only pushes data, more secure
    var createdAt: LocalDateTime = LocalDateTime.now()
)
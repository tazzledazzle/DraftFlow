package models

data class User(
    var id: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var password: String = "",
    var role: UserRole = UserRole.FOREMAN, // foreman role only pushes data, more secure
    var createdAt: String = ""
)
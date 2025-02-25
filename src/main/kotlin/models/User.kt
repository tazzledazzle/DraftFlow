package models

data class User (
    var id: String,
    var firstName: String,
    var lastName: String,
    var email: String,
    var password: String,
    var role: String,
    var createdAt: String
)
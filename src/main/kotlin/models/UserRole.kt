package com.northshore.models

enum class UserRole(var role: String) {
    ADMIN("ADMIN"),
    PROJECT_MANAGER("PROJECT_MANAGER"),
    DEVELOPER("DEVELOPER"),
    FOREMAN ("FOREMAN")
}

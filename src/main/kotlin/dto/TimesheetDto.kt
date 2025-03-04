package com.northshore.dto

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class LoginResponseDto (
    val token: String,
    val username: String,
    val role: String,
    val expiresAt: Long
)
data class ExcelTokenRequestDto (
    val projectId: Long
)

data class ExcelTokenValidationDto (
    val token: String
)
package com.northshore.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


data class LoginRequestDto (
    val username: String,
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

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    @field:NotBlank
    val username: String,

    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    @field:Size(min = 6, max = 40)
    val password: String
)

data class JwtResponse(
    val token: String,
    val id: Long,
    val username: String,
    val email: String,
    val roles: List<String>
)

data class MessageResponse(
    val message: String
)

data class ExcelTokenRequest(
    val projectId: Long
)

data class ExcelTokenResponse(
    val token: String?,
    val expiresIn : Long? = null,
    val projectId: Long? = null,
    val projectName: String? = null
)

data class TokenValidationRequest(
    val token: String
)

data class TokenValidationResponse(
    val valid: Boolean,
    val projectId: Long? = null
)
package com.northshore.controllers

import com.northshore.dto.ExcelTokenRequestDto
import com.northshore.dto.LoginRequestDto
import com.northshore.dto.LoginResponseDto
import com.northshore.dto.TokenValidationRequest
import com.northshore.dto.TokenValidationResponse
import com.northshore.services.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/auth")
class AuthApiController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequestDto): ResponseEntity<LoginResponseDto> {
        val loginResponse = authService.authenticate(loginRequest)
        return ResponseEntity.ok(loginResponse)
    }

    @PostMapping("/validate")
    fun validateToken(@Valid @RequestBody request: TokenValidationRequest): ResponseEntity<Boolean> {
        val validationResponse = authService.validateExcelToken(request.token)
        return ResponseEntity.ok(validationResponse)
    }

    @PostMapping("/refresh")
    fun refreshToken(@RequestHeader("Authorization") authHeader: String): ResponseEntity<LoginResponseDto> {
        // Extract token from Authorization header
        val token = authHeader.substring(7) // Remove "Bearer " prefix
        val refreshedToken = authService.refreshToken(token)
        return ResponseEntity.ok(refreshedToken)
    }
}
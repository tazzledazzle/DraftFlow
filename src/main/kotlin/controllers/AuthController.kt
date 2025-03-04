package com.northshore.controllers

import com.northshore.repository.UserRepository
import com.northshore.services.JwtTokenService
import jakarta.validation.Valid
import models.User
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenService: JwtTokenService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<JwtResponse> {
        // Authenticate user
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.username,
                loginRequest.password
            )
        )

        // Set authentication in security context
        SecurityContextHolder.getContext().authentication = authentication

        // Generate JWT token
        val jwt = jwtTokenService.generateToken(authentication)
        val userDetails = authentication.principal as UserDetailsImpl

        // Return response with token and user info
        return ResponseEntity.ok(
            JwtResponse(
                token = jwt,
                id = userDetails.id,
                username = userDetails.username,
                email = userDetails.email,
                roles = userDetails.authorities.map { it.authority }
            )
        )
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody registerRequest: RegisterRequest): ResponseEntity<*> {
        // Check if username is already taken
        if (userRepository.existsByUsername(registerRequest.username)) {
            return ResponseEntity
                .badRequest()
                .body(MessageResponse("Error: Username is already taken!"))
        }

        // Check if email is already in use
        if (userRepository.existsByEmail(registerRequest.email)) {
            return ResponseEntity
                .badRequest()
                .body(MessageResponse("Error: Email is already in use!"))
        }

        // Create new user
        val user = User(
            username = registerRequest.username,
            email = registerRequest.email,
            password = passwordEncoder.encode(registerRequest.password),
            role = "PROJECT_MANAGER" // Default role for now
        )

        userRepository.save(user)

        return ResponseEntity.ok(MessageResponse("User registered successfully!"))
    }
}
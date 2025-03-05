package com.northshore.controllers

import com.northshore.dto.ExcelTokenRequest
import com.northshore.dto.ExcelTokenResponse
import com.northshore.dto.TokenValidationRequest
import com.northshore.dto.TokenValidationResponse
import com.northshore.services.JwtTokenService
import org.apache.tomcat.util.net.openssl.ciphers.Authentication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import repository.ProjectRepository

@RestController
@RequestMapping("/api/excel-auth")
class ExcelAuthController(
    private val jwtTokenService: JwtTokenService,
    private val projectRepository: ProjectRepository
) {

    @PostMapping("/generate-token")
    fun generateExcelToken(
        @RequestBody request: ExcelTokenRequest,
        authentication: Authentication
    ): ResponseEntity<ExcelTokenResponse> {
        // Verify the project exists
        val projectExists = projectRepository.existsById(request.projectId)
        if (!projectExists) {
            return ResponseEntity.notFound().build()
        }

        // Generate Excel-specific token
        val token = jwtTokenService.generateExcelToken(request.projectId)

        return ResponseEntity.ok(ExcelTokenResponse(token = token))
    }

    @PostMapping("/validate")
    fun validateToken(@RequestBody request: TokenValidationRequest): ResponseEntity<TokenValidationResponse> {
        val token = request.token
        val isValid = jwtTokenService.validateToken(token)

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(TokenValidationResponse(valid = false))
        }

        // For Excel tokens, check if project ID is valid
        val projectId = jwtTokenService.getProjectIdFromExcelToken(token)
        if (projectId != null) {
            val projectExists = projectRepository.existsById(projectId)
            if (!projectExists) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(TokenValidationResponse(valid = false))
            }

            return ResponseEntity.ok(TokenValidationResponse(
                valid = true,
                projectId = projectId
            ))
        }

        return ResponseEntity.ok(TokenValidationResponse(valid = true))
    }
}
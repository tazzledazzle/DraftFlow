package com.northshore.controllers

import com.northshore.dto.ExcelTokenRequestDto
import com.northshore.dto.ExcelTokenResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/excel-auth")
class ExcelAuthApiController(private val excelAuthService: ExcelAuthService) {

    @PostMapping("/generate-token")
    fun generateExcelToken(
        @Valid @RequestBody request: ExcelTokenRequestDto
    ): ResponseEntity<ExcelTokenResponse> {
        val tokenResponse = excelAuthService.generateExcelToken(request.projectId)
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/validate")
    fun validateExcelToken(
        @Valid @RequestBody request: TokenValidationRequestDto
    ): ResponseEntity<TokenValidationResponseDto> {
        val validationResponse = excelAuthService.validateExcelToken(request.token)
        return ResponseEntity.ok(validationResponse)
    }

    @GetMapping("/project-info/{token}")
    fun getProjectInfoFromToken(@PathVariable token: String): ResponseEntity<ProjectInfoDto> {
        return excelAuthService.getProjectInfoFromToken(token)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }
}
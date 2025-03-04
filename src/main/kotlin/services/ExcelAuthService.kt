package com.northshore.services

import com.northshore.dto.ExcelTokenResponse
import com.northshore.dto.TokenValidationResponse
import com.northshore.exceptions.ResourceNotFoundException
import dto.ProjectInfo
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import repository.ProjectRepository
import java.util.*


/**
 * Service interface for handling Excel Add-In authentication.
 * Provides functionality for token generation, validation, and project information extraction.
 */
interface ExcelAuthService {
    /**
     * Generates a token for Excel Add-In authentication tied to a specific project.
     *
     * @param projectId The ID of the project to generate a token for
     * @return ExcelTokenResponseDto containing the token and project information
     */
    fun generateExcelToken(projectId: Long): ExcelTokenResponse

    /**
     * Validates an Excel-specific token.
     *
     * @param token The token to validate
     * @return TokenValidationResponseDto with validation results
     */
    fun validateExcelToken(token: String): TokenValidationResponse

    /**
     * Extracts project information from a valid Excel token.
     *
     * @param token The token to extract information from
     * @return ProjectInfoDto with project details, or null if token is invalid
     */
    fun getProjectInfoFromToken(token: String): ProjectInfo?

    /**
     * Refreshes an Excel token, extending its expiration time.
     *
     * @param token The token to refresh
     * @return A new token with extended expiration, or null if the token is invalid
     */
    fun refreshExcelToken(token: String): String?
}

@Service
class ExcelAuthServiceImpl(
    private val projectRepository: ProjectRepository
) : ExcelAuthService {

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.excel-expiration-ms}")
    private var jwtExcelExpirationMs: Long = 0

    /**
     * Generates a token specifically for Excel add-in authentication.
     * This token includes project-specific claims and has a longer expiration
     * to accommodate Excel usage patterns.
     *
     * @param projectId The ID of the project to generate a token for
     * @return ExcelTokenResponseDto containing the token and project information
     * @throws ResourceNotFoundException if the project doesn't exist
     */
    @Transactional
    override fun generateExcelToken(projectId: Long): ExcelTokenResponse {
        // Find the project
        val project = projectRepository.findById(projectId)
            .orElseThrow { ResourceNotFoundException("Project not found with id: $projectId") }

        // Generate token expiration date
        val now = Date()
        val expiryDate = Date(now.time + jwtExcelExpirationMs)

        // Create claims for the token
        val claims = Jwts.claims().setSubject("excel-client")
        claims["projectId"] = projectId
        claims["type"] = "excel"
        claims["iat"] = now.time / 1000  // Issued at time in seconds

        // Generate the token
        val token = Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(Keys.hmacShaKeyFor(jwtSecret.toByteArray()), SignatureAlgorithm.HS512)
            .compact()

        // Return response with token and project details
        return ExcelTokenResponse(
            token = token,
            expiresIn = jwtExcelExpirationMs / 1000,  // Convert to seconds
            projectId = project.id ?: projectId,
            projectName = project.name
        )
    }

    /**
     * Validates an Excel-specific token.
     *
     * @param token The token to validate
     * @return TokenValidationResponseDto with validation results
     */
    override fun validateExcelToken(token: String): TokenValidationResponse {
        try {
            // Parse and validate the token
            val claims = extractClaims(token)

            // Verify this is an Excel token
            if (claims["type"] != "excel") {
                return TokenValidationResponse(valid = false)
            }

            // Extract project ID
            val projectId = claims["projectId"] as? Long
                ?: return TokenValidationResponse(valid = false)

            // Verify the project exists
            val projectExists = projectRepository.existsById(projectId)
            if (!projectExists) {
                return TokenValidationResponse(valid = false)
            }

            // Token is valid
            return TokenValidationResponse(
                valid = true,
                projectId = projectId
            )
        } catch (e: Exception) {
            // Any exception means the token is invalid
            return TokenValidationResponse(valid = false)
        }
    }

    /**
     * Extracts project information from a valid Excel token.
     *
     * @param token The token to extract information from
     * @return ProjectInfoDto with project details, or null if token is invalid
     */
    @Transactional
    override fun getProjectInfoFromToken(token: String): ProjectInfo? {
        try {
            // Extract and validate the token
            val claims = extractClaims(token)

            // Verify this is an Excel token
            if (claims["type"] != "excel") {
                return null
            }

            // Extract project ID
            val projectId = claims["projectId"] as? Long ?: return null

            // Fetch the project
            val project = projectRepository.findById(projectId)
                .orElse(null) ?: return null

            // Return project info
            return ProjectInfo(
                id = project.id ?: projectId,
                name = project.name,
                description = project.description,
                startDate = project.startDate,
                endDate = project.endDate
            )
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Refreshes an Excel token, extending its expiration time.
     *
     * @param token The token to refresh
     * @return A new token with extended expiration, or null if the token is invalid
     */
    override fun refreshExcelToken(token: String): String? {
        try {
            // Extract claims from the existing token
            val claims = extractClaims(token)

            // Verify this is an Excel token
            if (claims["type"] != "excel") {
                return null
            }

            // Get the project ID
            val projectId = claims["projectId"] as? Long ?: return null

            // Generate a new token
            val now = Date()
            val expiryDate = Date(now.time + jwtExcelExpirationMs)

            return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.toByteArray()), SignatureAlgorithm.HS512)
                .compact()
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Helper method to extract and validate claims from a token.
     *
     * @param token The token to extract claims from
     * @return The claims object
     * @throws Exception if the token is invalid or expired
     */
    private fun extractClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.toByteArray()))
            .build()
            .parseClaimsJws(token)
            .body
    }
}
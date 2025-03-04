package com.northshore.services

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.apache.tomcat.util.net.openssl.ciphers.Authentication
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtTokenService {

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.expiration-ms}")
    private var jwtExpirationMs: Long = 0

    @Value("\${app.jwt.excel-expiration-ms}")
    private var jwtExcelExpirationMs: Long = 0

    /**
     * Generate JWT token from authentication object
     */
    fun generateToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserDetailsImpl
        return createToken(
            username = userPrincipal.username,
            authorities = userPrincipal.authorities,
            expirationMs = jwtExpirationMs
        )
    }

    /**
     * Generate a special token for Excel with project scope
     */
    fun generateExcelToken(projectId: Long): String {
        return Jwts.builder()
            .setSubject("excel-client")
            .claim("projectId", projectId)
            .claim("type", "excel")
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + jwtExcelExpirationMs))
            .signWith(Keys.hmacShaKeyFor(jwtSecret.toByteArray()), SignatureAlgorithm.HS512)
            .compact()
    }

    /**
     * Validate JWT token
     */
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.toByteArray()))
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Extract username from JWT token
     */
    fun getUsernameFromToken(token: String): String? {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.toByteArray()))
                .build()
                .parseClaimsJws(token)
                .body
                .subject
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extract project ID from Excel token
     */
    fun getProjectIdFromExcelToken(token: String): Long? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.toByteArray()))
                .build()
                .parseClaimsJws(token)
                .body

            val tokenType = claims["type"] as? String
            if (tokenType == "excel") {
                claims["projectId"] as? Long
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Private helper methods
    private fun createToken(
        username: String,
        authorities: Collection<GrantedAuthority>,
        expirationMs: Long
    ): String {
        val claims = Jwts.claims().setSubject(username)
        claims["roles"] = authorities.map { it.authority }

        val now = Date()
        val expiration = Date(now.time + expirationMs)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(Keys.hmacShaKeyFor(jwtSecret.toByteArray()), SignatureAlgorithm.HS512)
            .compact()
    }
}
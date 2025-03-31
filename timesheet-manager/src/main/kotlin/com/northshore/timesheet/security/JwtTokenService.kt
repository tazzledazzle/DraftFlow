package com.northshore.timesheet.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtTokenService {

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.expiration-ms}")
    private var jwtExpirationMs: Long = 0

    @Value("\${app.jwt.excel-expiration-ms}")
    private var jwtExcelExpirationMs: Long = 0

    fun generateToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as User

        val claims = Jwts.claims().setSubject(userPrincipal.username)
        claims["roles"] = userPrincipal.authorities.map { it.authority }

        val now = Date()
        val expiration = Date(now.time + jwtExpirationMs)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(Keys.hmacShaKeyFor(jwtSecret.toByteArray()), SignatureAlgorithm.HS512)
            .compact()
    }

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

    fun validateToken(token: String): Boolean {
        try {
            Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.toByteArray()))
                .build()
                .parseClaimsJws(token)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun getAuthentication(token: String): Authentication {
        val claims = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.toByteArray()))
            .build()
            .parseClaimsJws(token)
            .body

        val username = claims.subject

        val authorities = when {
            claims.get("roles") != null -> {
                @Suppress("UNCHECKED_CAST")
                (claims.get("roles") as List<String>).map { SimpleGrantedAuthority(it) }
            }
            claims.get("projectId") != null -> {
                listOf(SimpleGrantedAuthority("ROLE_EXCEL_CLIENT"))
            }
            else -> emptyList()
        }

        val principal = User(username, "", authorities)
        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    fun getProjectIdFromToken(token: String): Long? {
        try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.toByteArray()))
                .build()
                .parseClaimsJws(token)
                .body

            return claims.get("projectId", Long::class.java)
        } catch (e: Exception) {
            return null
        }
    }
}
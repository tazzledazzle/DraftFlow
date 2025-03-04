package com.northshore.db

import com.northshore.services.JwtTokenService
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import kotlin.jvm.java

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val userDetailsService: UserDetailsService,
    private val jwtTokenFilter: JwtTokenFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Public endpoints
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/excel-auth/**").permitAll()
                    // Protected endpoints
                    .requestMatchers("/api/projects/**").hasRole("PROJECT_MANAGER")
                    .requestMatchers("/api/tasks/**").hasRole("PROJECT_MANAGER")
                    .requestMatchers("/api/timesheets/submit").permitAll() // Excel submissions
                    .requestMatchers("/api/timesheets/**").hasRole("PROJECT_MANAGER")
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(
        config: AuthenticationConfiguration
    ): AuthenticationManager = config.authenticationManager
}

@Component
class JwtTokenFilter(private val jwtTokenService: JwtTokenService) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            // Get JWT token from Authorization header
            val token = getTokenFromRequest(request)

            // Validate token and set authentication
            if (token != null && jwtTokenService.validateToken(token)) {
                setAuthentication(token, request)
            }
        } catch (e: Exception) {
            // Log but don't stop the request - authentication will remain null
            logger.error("Cannot set user authentication: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }

    private fun getTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")

        return if (!bearerToken.isNullOrBlank() && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }

    private fun setAuthentication(token: String, request: HttpServletRequest) {
        // Regular web authentication
        val username = jwtTokenService.getUsernameFromToken(token)
        if (username != null) {
            val userDetails = CustomUserDetailsService.loadUserByUsername(username)
            val authentication = UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.authorities
            )
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
            return
        }

        // Excel token authentication (project-specific)
        val projectId = jwtTokenService.getProjectIdFromExcelToken(token)
        if (projectId != null) {
            val authorities = listOf(SimpleGrantedAuthority("EXCEL_SUBMITTER"))
            val authentication = UsernamePasswordAuthenticationToken(
                "excel-client", null, authorities
            )
            authentication.details = mapOf("projectId" to projectId)
            SecurityContextHolder.getContext().authentication = authentication
        }
    }
}
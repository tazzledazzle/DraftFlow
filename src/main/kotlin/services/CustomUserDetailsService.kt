package com.northshore.services

import com.northshore.controllers.ApplicationContextProvider
import com.northshore.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    @Transactional
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username).get()
            ?: throw UsernameNotFoundException("User not found with username: $username")

        return UserDetailsImpl(user)
    }

    companion object {
        /**
         * Static method to be used in the JwtTokenFilter
         */
        @JvmStatic
        fun loadUserDetails(username: String): UserDetails {
            // This is a static implementation for use in the filter
            // In a real implementation, we would use application context or another mechanism
            // to avoid this static approach

            // This is just for demonstration - in a real app you'd use a proper
            // service location mechanism
            val userRepository = ApplicationContextProvider.getBean(UserRepository::class.java)
            val user = userRepository.findByUsername(username).get()
                ?: throw UsernameNotFoundException("User not found with username: $username")

            return UserDetailsImpl(user)
        }
    }
}
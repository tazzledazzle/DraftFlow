package com.northshore.repository

import com.northshore.models.User
import com.northshore.models.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Repository interface for User entity access
 */
@Repository
interface UserRepository : JpaRepository<User, Long> {
    /**
     * Finds a user by username
     * @param username The username to look for
     * @return Optional containing the user if found
     */
    fun findByUsername(username: String): Optional<User>

    /**
     * Finds a user by email
     * @param email The email to look for
     * @return Optional containing the user if found
     */
    fun findByEmail(email: String): Optional<User>

    /**
     * Checks if a username exists
     * @param username The username to check
     * @return true if the username exists, false otherwise
     */
    fun existsByUsername(username: String): Boolean

    /**
     * Checks if an email exists
     * @param email The email to check
     * @return true if the email exists, false otherwise
     */
    fun existsByEmail(email: String): Boolean

    /**
     * Finds users by role
     * @param role The role to search for
     * @return List of users with this role
     */
    fun findByRole(role: UserRole): List<User>
}
package com.northshore.timesheet.repository

import com.northshore.timesheet.entity.User
import org.apache.el.stream.Optional
import org.springframework.data.jpa.repository.JpaRepository

// src/main/kotlin/com/yourcompany/timesheet/repository/UserRepository.kt


interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): Optional<User>
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
}
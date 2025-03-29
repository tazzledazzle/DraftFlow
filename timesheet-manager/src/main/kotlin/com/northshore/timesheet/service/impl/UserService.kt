package com.northshore.timesheet.service.impl

import com.northshore.timesheet.entity.User
import org.apache.el.stream.Optional


interface UserService {
    fun findByUsername(username: String): Optional<User>
    fun findById(id: Long): Optional<User>
    fun getAllUsers(): List<User>
    fun createUser(user: User): User
}
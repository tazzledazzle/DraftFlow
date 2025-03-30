package com.northshore.timesheet.service.impl

import com.northshore.timesheet.entity.User
import com.northshore.timesheet.repository.UserRepository
import org.apache.el.stream.Optional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


interface UserService {
    fun findByUsername(username: String): User?
    fun findById(id: Long): User?
    fun getAllUsers(): List<User>
    fun createUser(user: User): User?
}

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : UserService {

    override fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    override fun findById(id: Long): User? {
        return userRepository.findById(id).get()
    }

    override fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    override fun createUser(user: User): User? {
        // Encode password before saving
        user.password = passwordEncoder.encode(user.password)
        return userRepository.save(user)
    }
}
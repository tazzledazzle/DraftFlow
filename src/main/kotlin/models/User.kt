package com.northshore.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,
    @Column
    var firstName: String = "",
    @Column
    var lastName: String = "",
    @Column
    var username: String = "",

    @Column(nullable = false)
    var email: String = "",
    @Column(nullable = false)
    var password: String = "",
    @Column
    var role: UserRole = UserRole.FOREMAN, // foreman role only pushes data, more secure
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
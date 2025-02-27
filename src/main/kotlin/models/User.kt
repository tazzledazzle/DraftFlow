package models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: String = "",
    @Column
    var firstName: String = "",
    @Column
    var lastName: String = "",
    @Column
    var email: String = "",
    @Column
    var password: String = "",
    @Column
    var role: UserRole = UserRole.FOREMAN, // foreman role only pushes data, more secure
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
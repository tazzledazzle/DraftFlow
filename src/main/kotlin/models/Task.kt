package models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "tasks")
data class Task (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var projectId: Long = 0L,
    @Column
    var name: String = "",
    @Column
    var description: String = "",
    @Column()
    var estimatedHours: Double = 0.0,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: String = ""
)
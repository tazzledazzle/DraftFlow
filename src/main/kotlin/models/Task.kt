package models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.time.LocalDateTime

@Entity
@Table(name = "tasks")
data class Task (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var projectId: Long = 0L, //todo: Project Object
    @Column
    var name: String = "",
    @Column
    var description: String = "",
    @Column
    var estimatedHours: Double = 0.0,
    /**
     * The percentage of the task that has been completed.
     * This is a value between 0 and 100.
     */
    @Column
    var progress: Double = 0.0,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt:  LocalDateTime? = null
)
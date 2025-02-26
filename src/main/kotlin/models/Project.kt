package models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "projects")
data class Project (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: String = "",

    @Column(nullable = false)
    var projectManagerId: String = "",

    @Column
    var name: String = "",

    @Column
    var description: String = "",

    @Column(name = "start_date")
    var startDate: String = "",

    @Column(name = "end_date")
    var endDate: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_manager_id")
    var projectManager: User? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
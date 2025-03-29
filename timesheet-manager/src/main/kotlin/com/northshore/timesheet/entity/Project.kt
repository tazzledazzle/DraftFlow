// src/main/kotlin/com/yourcompany/timesheet/entity/Project.kt
package com.northshore.timesheet.entity

import jakarta.persistence.*
import kotlinx.datetime.Clock.System
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.hibernate.annotations.CascadeType


@Entity
class Project(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String,

    @Column
    var description: String? = null,

    @Column(name = "start_date")
    var startDate: LocalDate? = null,

    @Column(name = "end_date")
    var endDate: LocalDate? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_manager_id")
    var projectManager: User? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = System.now().toLocalDateTime(TimeZone.UTC),

    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true)
    val tasks: MutableList<Task> = mutableListOf()
) {
    // Utility methods
    fun addTask(task: Task) {
        tasks.add(task)
        task.project = this
    }

    fun removeTask(task: Task) {
        tasks.remove(task)
        task.project = null
    }
}
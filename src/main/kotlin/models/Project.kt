package com.northshore.models

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "projects")
data class Project (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    @Column(name = "project_manager_id", nullable = false)
    var projectManagerId: Long = 0L, // todo: User Object

    @Column
    var name: String = "",

    @Column
    var description: String = "",

    @Column(name = "start_date")
    var startDate:  LocalDate? = null,

    @Column(name = "end_date")
    var endDate:  LocalDate? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_manager_id", nullable = false, insertable = false, updatable = false)
    var projectManager: User? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),


    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true)
    var tasks: List<Task> = mutableListOf()
) {
    fun addTask(task: Task) {
        tasks += task
        task.projectId = this.id
    }

    fun removeTask(task: Task) {
        tasks -= task
        task.projectId = 0L
    }
}
package models

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "projects")
data class Project (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    @Column(nullable = false)
    var projectManagerId: Long = 0L,

    @Column
    var name: String = "",

    @Column
    var description: String = "",

    @Column(name = "start_date")
    var startDate:  LocalDateTime? = null,

    @Column(name = "end_date")
    var endDate:  LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_manager_id")
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
package repository

import models.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TaskRepository : JpaRepository<Task, Long> {
    fun findByProjectId(projectId: Long): List<Task>

    @Query("SELECT COUNT(t) FROM Task t WHERE t.projectId = :projectId")
    fun countTasksByProjectId(@Param("projectId") projectId: Long): Long
}

package com.northshore.timesheet.repository

import com.northshore.timesheet.entity.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param


// src/main/kotlin/com/yourcompany/timesheet/repository/TaskRepository.kt

interface TaskRepository : JpaRepository<Task, Long> {
    fun findByProjectId(projectId: Long): List<Task>

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    fun countTasksByProjectId(@Param("projectId") projectId: Long): Long
}
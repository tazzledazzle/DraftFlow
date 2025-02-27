package repository

import models.Project
import models.Task
import models.TimesheetEntry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface ProjectRepository : JpaRepository<Project, Long> {
    fun findByProjectManagerId(projectManagerId: Long): List<Project>

    fun findByNameContainingIgnoreCase(name: String): List<Project>

    @Query("SELECT p FROM Project p WHERE p.startDate <= :date AND (p.endDate IS NULL OR p.endDate >= :date)")
    fun findActiveProjects(@Param("date") date: LocalDate): List<Project>
}

interface TaskRepository : JpaRepository<Task, Long> {
    fun findByProjectId(projectId: Long): List<Task>

    @Query("SELECT COUNT(t) FROM Task t WHERE t.projectId = :projectId")
    fun countTasksByProjectId(@Param("projectId") projectId: Long): Long
}

interface TimesheetEntryRepository : JpaRepository<TimesheetEntry, Long> {
    fun findByTaskId(taskId: Long): List<TimesheetEntry>

    fun findByTaskProjectId(projectId: Long): List<TimesheetEntry>

    fun findByWorkDateBetween(startDate: LocalDate, endDate: LocalDate): List<TimesheetEntry>

    @Query("SELECT NEW com.yourcompany.timesheet.dto.TaskHoursDto(t.id, t.name, SUM(te.hoursWorked)) " +
            "FROM Task t JOIN t.timesheetEntries te " +
            "WHERE t.project.id = :projectId " +
            "GROUP BY t.id, t.name")
    fun getTaskHoursByProject(@Param("projectId") projectId: Long): List<TaskHoursDto>
}


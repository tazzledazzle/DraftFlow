package repository

import dto.TaskHoursDto
import models.Project
import models.Task
import models.TimesheetEntry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface ProjectRepository : JpaRepository<Project, Long> {
    fun findByProjectManagerId(projectManagerId: Long): List<Project>

    fun findProjectById(id: Long): Project?
    fun findByNameContainingIgnoreCase(name: String): List<Project>

    @Query("SELECT p FROM Project p WHERE p.startDate <= :date AND (p.endDate IS NULL OR p.endDate >= :date)")
    fun findActiveProjects(@Param("date") date: LocalDate): List<Project>

}




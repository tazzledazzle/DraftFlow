package services

import com.northshore.services.ProjectService
import controllers.ProjectApiController
import models.Project
import org.mockito.Mockito.mock
import java.time.LocalDate
import java.time.LocalDateTime

class ProjectTestController(private val projectService: ProjectService) : ProjectApiController( mock(ProjectService::class.java) ) {
    companion object {
        fun getProjects() {
            TODO()
        }

        fun createProject(): Project {
            return Project(
                id = 1,
                projectManagerId = 1,
                name = "Test Project 1",
                description = "Description 1",
                startDate = LocalDateTime.of(2021, 1, 1, 0, 0) as LocalDate?,
                endDate = LocalDateTime.of(2021, 12, 31, 0, 0) as LocalDate?,
                createdAt = LocalDateTime.of(2025, 2, 27, 0, 0)
            )
        }

        fun getProjectById(id: Long): Project? {
            TODO()
        }

        fun updateProject() {
            TODO()
        }

        fun deleteProject() {
            TODO()
        }
    }
}
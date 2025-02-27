package services

import models.Project
import java.time.LocalDateTime

class ProjectTestController(private val projectService: ProjectService) {

    fun getProjects() {
        TODO()
    }

    fun createProject(): Project {
        return Project(
            id = 1,
            projectManagerId = 1,
            name = "Test Project 1",
            description = "Description 1",
            startDate = LocalDateTime.of(2021, 1, 1, 0, 0),
            endDate = LocalDateTime.of(2021, 12, 31, 0, 0),
            createdAt = LocalDateTime.now()
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
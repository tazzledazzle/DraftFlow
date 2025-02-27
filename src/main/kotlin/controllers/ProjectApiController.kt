package controllers

import models.Project
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import services.ProjectService

/*
            * Endpoint	Method	Description	Request	Response
            /api/projects	GET	Get all projects	-	Array of ProjectDto
            /api/projects	POST	Create new project	ProjectCreateDto	ProjectDto
            /api/projects/{id}	GET	Get project by ID	-	ProjectDto
            /api/projects/{id}	PUT	Update project	ProjectUpdateDto	ProjectDto
            /api/projects/{id}	DELETE	Delete project	-	204 No Content
*
* */


class ProjectApiController(private val projectService: ProjectService) {

    @GetMapping("/api/projects")
    fun getProjects() {
        TODO()
    }

    @PostMapping("/api/projects")
    fun createProject() {
        TODO()
    }

    @GetMapping("/api/projects/{id}")
    fun getProjectById(@PathVariable id: Long): Project? {
        TODO()
    }

    @PutMapping("/api/projects/{id}")
    fun updateProject() {
        TODO()
    }

    @DeleteMapping("/api/projects/{id}")
    fun deleteProject() {
        TODO()
    }
}
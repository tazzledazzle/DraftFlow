package com.northshore.timesheet.controller

import com.northshore.timesheet.dto.ProjectDto
import com.northshore.timesheet.dto.ProjectCreateDto
import com.northshore.timesheet.dto.ProjectUpdateDto
import com.northshore.timesheet.service.impl.ProjectService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
//import org.springframework.web.bind.annotation.Valid

@RestController
@RequestMapping("/api/projects")
class ProjectApiController(private val projectService: ProjectService) {

    @GetMapping
    fun getAllProjects(): ResponseEntity<List<ProjectDto>> {
        val projects = projectService.getAllProjects()
        return ResponseEntity.ok(projects)
    }

    @GetMapping("/{id}")
    fun getProject(@PathVariable id: Long): ResponseEntity<ProjectDto> {
        return projectService.getProjectById(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/manager/{managerId}")
    fun getProjectsByManager(@PathVariable managerId: Long): ResponseEntity<List<ProjectDto>> {
        val projects = projectService.getProjectsByManager(managerId)
        return ResponseEntity.ok(projects)
    }

    @GetMapping("/search")
    fun searchProjects(@RequestParam name: String): ResponseEntity<List<ProjectDto>> {
        val projects = projectService.findProjectsByName(name)
        return ResponseEntity.ok(projects)
    }

    @GetMapping("/active")
    fun getActiveProjects(): ResponseEntity<List<ProjectDto>> {
        val projects = projectService.getActiveProjects()
        return ResponseEntity.ok(projects)
    }

    @PostMapping
    fun createProject( @RequestBody projectDto: ProjectCreateDto): ResponseEntity<ProjectDto> {
        val created = projectService.createProject(projectDto)
        return ResponseEntity
            .created(URI.create("/api/projects/${created.id}"))
            .body(created)
    }

    @PutMapping("/{id}")
    fun updateProject(
        @PathVariable id: Long,
        @RequestBody projectDto: ProjectUpdateDto
    ): ResponseEntity<ProjectDto> {
        return projectService.updateProject(id, projectDto)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}")
    fun deleteProject(@PathVariable id: Long): ResponseEntity<Void> {
        val deleted = projectService.deleteProject(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
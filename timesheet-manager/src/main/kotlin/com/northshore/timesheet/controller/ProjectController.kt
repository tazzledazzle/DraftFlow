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
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/projects")
class ProjectController(
    private val projectService: ProjectService,
    private val taskService: TaskService,
    private val userService: UserService
) {

    @GetMapping
    fun listProjects(model: Model): String {
        model.addAttribute("projects", projectService.getAllProjects())
        return "projects/list"
    }

    @GetMapping("/create")
    fun createProjectForm(model: Model): String {
        model.addAttribute("project", ProjectCreateDto(name = ""))
        model.addAttribute("projectManagers", userService.getAllUsers())
        return "projects/create"
    }

    @PostMapping("/create")
    fun createProject(
        @Valid @ModelAttribute("project") projectDto: ProjectCreateDto,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute("projectManagers", userService.getAllUsers())
            return "projects/create"
        }

        val created = projectService.createProject(projectDto)
        redirectAttributes.addFlashAttribute("message", "Project created successfully")
        return "redirect:/projects/${created.id}"
    }

    @GetMapping("/{id}")
    fun viewProject(@PathVariable id: Long, model: Model): String {
        val project = projectService.getProjectById(id)
            ?: return "redirect:/projects"

        model.addAttribute("project", project)
        model.addAttribute("tasks", taskService.getTasksByProject(id))
        return "projects/view"
    }

    @GetMapping("/{id}/edit")
    fun editProjectForm(@PathVariable id: Long, model: Model): String {
        val project = projectService.getProjectById(id)
            ?: return "redirect:/projects"

        model.addAttribute("project", ProjectUpdateDto(
            name = project.name,
            description = project.description,
            startDate = project.startDate,
            endDate = project.endDate,
            projectManagerId = project.projectManagerId
        ))
        model.addAttribute("projectId", id)
        model.addAttribute("projectManagers", userService.getAllUsers())
        return "projects/edit"
    }

    @PostMapping("/{id}/edit")
    fun updateProject(
        @PathVariable id: Long,
        @Valid @ModelAttribute("project") projectDto: ProjectUpdateDto,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute("projectId", id)
            model.addAttribute("projectManagers", userService.getAllUsers())
            return "projects/edit"
        }

        projectService.updateProject(id, projectDto)
        redirectAttributes.addFlashAttribute("message", "Project updated successfully")
        return "redirect:/projects/${id}"
    }

    @PostMapping("/{id}/delete")
    fun deleteProject(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        projectService.deleteProject(id)
        redirectAttributes.addFlashAttribute("message", "Project deleted successfully")
        return "redirect:/projects"
    }

    @GetMapping("/{id}/excel-token")
    fun generateExcelToken(@PathVariable id: Long, model: Model): String {
        // This will be implemented when we add security features
        model.addAttribute("projectId", id)
        model.addAttribute("token", "sample-token-placeholder")
        return "projects/excel-token"
    }
}
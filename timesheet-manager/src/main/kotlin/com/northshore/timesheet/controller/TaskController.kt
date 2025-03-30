package com.northshore.timesheet.controller

import com.northshore.timesheet.dto.TaskCreateDto
import com.northshore.timesheet.dto.TaskUpdateDto
import com.northshore.timesheet.service.impl.ProjectService
import com.northshore.timesheet.service.impl.TaskService
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/projects/{projectId}/tasks")
class TaskController(
    private val taskService: TaskService,
    private val projectService: ProjectService
) {

    @GetMapping("/create")
    fun createTaskForm(@PathVariable projectId: Long, model: Model): String {
        val project = projectService.getProjectById(projectId)
            ?: return "redirect:/projects"

        model.addAttribute("project", project)
        model.addAttribute("task", TaskCreateDto(
            projectId = projectId,
            name = "",
            description = null,
            estimatedHours = null
        ))
        return "tasks/create"
    }

    @PostMapping("/create")
    fun createTask(
        @PathVariable projectId: Long,
        @Valid @ModelAttribute("task") taskDto: TaskCreateDto,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            val project = projectService.getProjectById(projectId)
                ?: return "redirect:/projects"

            model.addAttribute("project", project)
            return "tasks/create"
        }

        taskService.createTask(taskDto)
        redirectAttributes.addFlashAttribute("message", "Task created successfully")
        return "redirect:/projects/${projectId}"
    }

    @GetMapping("/{taskId}/edit")
    fun editTaskForm(
        @PathVariable projectId: Long,
        @PathVariable taskId: Long,
        model: Model
    ): String {
        val task = taskService.getTaskById(taskId)
            ?: return "redirect:/projects/${projectId}"

        val project = projectService.getProjectById(projectId)
            ?: return "redirect:/projects"

        model.addAttribute("project", project)
        model.addAttribute("task", TaskUpdateDto(
            name = task.name,
            description = task.description,
            estimatedHours = task.estimatedHours
        ))
        model.addAttribute("taskId", taskId)
        return "tasks/edit"
    }

    @PostMapping("/{taskId}/edit")
    fun updateTask(
        @PathVariable projectId: Long,
        @PathVariable taskId: Long,
        @Valid @ModelAttribute("task") taskDto: TaskUpdateDto,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            val project = projectService.getProjectById(projectId)
                ?: return "redirect:/projects"

            model.addAttribute("project", project)
            model.addAttribute("taskId", taskId)
            return "tasks/edit"
        }

        taskService.updateTask(taskId, taskDto)
        redirectAttributes.addFlashAttribute("message", "Task updated successfully")
        return "redirect:/projects/${projectId}"
    }

    @PostMapping("/{taskId}/delete")
    fun deleteTask(
        @PathVariable projectId: Long,
        @PathVariable taskId: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        taskService.deleteTask(taskId)
        redirectAttributes.addFlashAttribute("message", "Task deleted successfully")
        return "redirect:/projects/${projectId}"
    }
}
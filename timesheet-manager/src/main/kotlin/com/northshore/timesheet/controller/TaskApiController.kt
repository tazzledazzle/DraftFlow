package com.northshore.timesheet.controller

import com.northshore.timesheet.dto.TaskCreateDto
import com.northshore.timesheet.dto.TaskDto
import com.northshore.timesheet.dto.TaskUpdateDto
import com.northshore.timesheet.service.impl.TaskService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/tasks")
class TaskApiController(private val taskService: TaskService) {

    @GetMapping("/project/{projectId}")
    fun getTasksByProject(@PathVariable projectId: Long): ResponseEntity<List<TaskDto>> {
        val tasks = taskService.getTasksByProject(projectId)
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/{id}")
    fun getTask(@PathVariable id: Long): ResponseEntity<TaskDto> {
        return taskService.getTaskById(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/dropdown/project/{projectId}")
    fun getTasksForDropdown(@PathVariable projectId: Long): ResponseEntity<List<String>> {
        val tasks = taskService.getTasksForDropdown(projectId)
        return ResponseEntity.ok(tasks)
    }

    @PostMapping
    fun createTask(@Valid @RequestBody taskDto: TaskCreateDto): ResponseEntity<TaskDto> {
        val created = taskService.createTask(taskDto)
        return ResponseEntity
            .created(URI.create("/api/tasks/${created.id}"))
            .body(created)
    }

    @PutMapping("/{id}")
    fun updateTask(
        @PathVariable id: Long,
        @Valid @RequestBody taskDto: TaskUpdateDto
    ): ResponseEntity<TaskDto> {
        return taskService.updateTask(id, taskDto)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Void> {
        val deleted = taskService.deleteTask(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
package controllers

/*
*
    | Endpoint | Method | Description | Request | Response |
    |   ---    |   ---  |    ---      | --- | --- |
    | /api/tasks/project/{projectId} | GET | Get tasks for project | - | Array of TaskDto |
    | /api/tasks | POST | Create new task | TaskCreateDto | TaskDto |
    | /api/tasks/{id} | GET | Get task by ID | - | TaskDto |
    | /api/tasks/{id} | PUT | Update task | TaskUpdateDto | TaskDto |
    | /api/tasks/{id} | DELETE | Delete task | - | 204 No Content |

*
* */

import com.northshore.dto.TaskHoursDto
import dto.TaskCreateDto
import dto.TaskDto
import dto.TaskUpdateDto
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import services.TaskService
import java.net.URI

@RestController
@RequestMapping("/api/tasks")
class TaskApiController(private val taskService: TaskService) {

    @GetMapping("/project/{projectId}")
    fun getTasksByProject(@PathVariable projectId: Long): ResponseEntity<List<TaskDto>> {
        val tasks = taskService.getTasksByProjectId(projectId)
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/{id}")
    fun getTask(@PathVariable id: Long): ResponseEntity<TaskDto> {
        return taskService.getTaskById(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
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

    @GetMapping("/project/{projectId}/dropdown")
    fun getTasksForDropdown(@PathVariable projectId: Long): ResponseEntity<List<TaskDto>> {
        // should be all the project tasks
        val taskOptions = taskService.getTasksByProjectId(projectId)
        return ResponseEntity.ok(taskOptions)
    }

    @GetMapping("/search")
    fun searchTasks(
        @RequestParam(required = false) projectId: Long?,
        @RequestParam(required = false) query: String?
    ): ResponseEntity<List<TaskDto>> {
        val tasks = taskService.getTasksByProjectId(projectId!!).find {
            it.name.contains(query ?: "", ignoreCase = true)
        }!!.let { listOf(it) }
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/{id}/progress")
    fun getTaskProgress(@PathVariable id: Long): ResponseEntity<TaskProgressDto> {
        return taskService.getTaskProgress(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }
}
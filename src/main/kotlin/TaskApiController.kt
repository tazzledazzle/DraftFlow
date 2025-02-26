import dto.TaskCreateDto
import dto.TaskUpdateDto
import jakarta.validation.Valid
import models.Task
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import services.TaskService
import java.net.URI

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
class TaskApiController(private val taskService: TaskService) {

    @GetMapping("/project/{projectId}")
    fun getTasksByProject(@PathVariable projectId: Long): ResponseEntity<List<Task>> {
        val tasks = taskService.getTasksByProject(projectId)
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/{id}")
    fun getTask(@PathVariable id: Long): ResponseEntity<Task> {
        return taskService.getTaskById(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @PostMapping
    fun createTask(@Valid @RequestBody Task: TaskCreateDto): ResponseEntity<Task> {
        val created = taskService.createTask(Task)
        return ResponseEntity
            .created(URI.create("/api/tasks/${created.id}"))
            .body(created)
    }

    @PutMapping("/{id}")
    fun updateTask(
        @PathVariable id: Long,
        @Valid @RequestBody Task: TaskUpdateDto
    ): ResponseEntity<Task> {
        return taskService.updateTask(id, Task)
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
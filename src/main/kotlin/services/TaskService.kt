package services

import dto.TaskCreateDto
import dto.TaskDto
import dto.TaskProgressDto
import dto.TaskUpdateDto
import models.Task
import org.springframework.transaction.annotation.Transactional
import repository.ProjectRepository
import repository.TaskRepository

interface TaskService {
    fun getTasksByProjectId(projectId: Long) : List<TaskDto>

    fun getTaskById(taskId: Long): TaskDto?

    fun createTask(taskCreateDto: TaskCreateDto): TaskDto

    fun updateTask(projectId: Long, taskUpdateDto: TaskUpdateDto): TaskDto?

    fun deleteTask(taskId: Long) : Boolean
    fun getTaskProgress(taskId: Long): TaskProgressDto
}
// todo: I need to figure out which id I want to use to manage these things, the project or task id
open class TaskServiceImpl(private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository) : TaskService {
    override fun getTasksByProjectId(projectId: Long): List<TaskDto> {
        return projectRepository.findProjectById(projectId)?.tasks?.map { it.toDto() } ?: emptyList()
    }

    override fun getTaskById(taskId: Long): TaskDto? {
        return taskRepository.getTaskById(taskId)?.toDto()
    }

    @Transactional
    override fun createTask(taskCreateDto: TaskCreateDto): TaskDto {
        validateTaskData (
            taskCreateDto.name,
            taskCreateDto.projectId,
            taskCreateDto.estimatedHours
        )

        val task = Task(
            projectId = taskCreateDto.projectId,
            name = taskCreateDto.name,
            description = taskCreateDto.description,
            estimatedHours = taskCreateDto.estimatedHours!!
        )

        val savedTask = taskRepository.save(task)
        return savedTask.toDto()
    }

    private fun validateTaskData(name: String, projectId: Long, estimatedHours: Double?) {
        if (name.isBlank()) {
            throw IllegalArgumentException("Name is required")
        }
        if (projectId <= 0) {
            throw IllegalArgumentException("Project ID is required")
        }
        if (estimatedHours != null && estimatedHours < 0) {
            throw IllegalArgumentException("Estimated hours must be 0 or greater")
        }
    }

    @Transactional
    override fun updateTask(projectId: Long, taskUpdateDto: TaskUpdateDto): TaskDto? {
        val task = projectRepository.findProjectById(projectId)?.tasks?.find { it.name == taskUpdateDto.name }
            ?: throw IllegalArgumentException("Task not found with name: ${taskUpdateDto.name}")

        validateTaskData(
            taskUpdateDto.name,
            taskUpdateDto.projectId,
            taskUpdateDto.estimatedHours
        )

        task.name = taskUpdateDto.name
        task.description = taskUpdateDto.description
        task.estimatedHours = taskUpdateDto.estimatedHours!!

        return taskRepository.save(task).toDto()
    }

    @Transactional
    override fun deleteTask(taskId: Long): Boolean {
        return if (taskRepository.existsById(taskId)) {
            taskRepository.deleteById(taskId)
            true
        } else {
            false
    }


}

    override fun getTaskProgress(taskId: Long): TaskProgressDto {
        val task = taskRepository.getTaskById(taskId)
            ?: throw IllegalArgumentException("Task not found with ID: $taskId")
        return TaskProgressDto(
            id = task.id,
            projectId = task.projectId,
            name = task.name,
            estimatedHours = task.estimatedHours,
            progress = task.progress
        )
    }
}

fun Task.toDto(): TaskDto {
    return TaskDto(
        id = this.id,
        projectId = this.projectId,
        name = this.name,
        description = this.description,
        estimatedHours = this.estimatedHours,
    )
}
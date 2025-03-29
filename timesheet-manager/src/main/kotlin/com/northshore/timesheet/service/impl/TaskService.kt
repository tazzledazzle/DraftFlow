package com.northshore.timesheet.service.impl

import com.northshore.timesheet.dto.TaskCreateDto
import com.northshore.timesheet.dto.TaskDto
import com.northshore.timesheet.dto.TaskUpdateDto
import com.northshore.timesheet.repository.ProjectRepository
import com.northshore.timesheet.repository.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


interface TaskService {
    fun getTasksByProject(projectId: Long): List<TaskDto>
    fun getTaskById(id: Long): TaskDto?
    fun createTask(taskDto: TaskCreateDto): TaskDto
    fun updateTask(id: Long, taskDto: TaskUpdateDto): TaskDto?
    fun deleteTask(id: Long): Boolean
    fun getTasksForDropdown(projectId: Long): List<String>
}


@Service
class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository
) : TaskService {

    override fun getTasksByProject(projectId: Long): List<TaskDto> {
        return taskRepository.findByProjectId(projectId)
            .map { it.toDto() }
    }

    override fun getTaskById(id: Long): TaskDto? {
        return taskRepository.findById(id)
            .map { it.toDto() }
            .orElse(null)
    }

    @Transactional
    override fun createTask(taskDto: TaskCreateDto): TaskDto {
        // Find the project
        val project = projectRepository.findById(taskDto.projectId)
            .orElseThrow { ResourceNotFoundException("Project not found with id: ${taskDto.projectId}") }

        // Create and save the task
        val task = taskDto.toEntity(project)
        val savedTask = taskRepository.save(task)
        return savedTask.toDto()
    }

    @Transactional
    override fun updateTask(id: Long, taskDto: TaskUpdateDto): TaskDto? {
        return taskRepository.findById(id).map { task ->
            // Update task properties
            task.name = taskDto.name
            task.description = taskDto.description
            task.estimatedHours = taskDto.estimatedHours

            // Save and convert to DTO
            taskRepository.save(task).toDto()
        }.orElse(null)
    }

    @Transactional
    override fun deleteTask(id: Long): Boolean {
        return if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    override fun getTasksForDropdown(projectId: Long): List<String> {
        return getTasksByProject(projectId)
            .map { "${it.id} - ${it.name}" }
    }
}
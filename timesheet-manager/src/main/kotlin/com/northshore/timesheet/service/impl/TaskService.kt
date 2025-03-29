package com.northshore.timesheet.service.impl

import com.northshore.timesheet.dto.TaskCreateDto
import com.northshore.timesheet.dto.TaskDto
import com.northshore.timesheet.dto.TaskUpdateDto


interface TaskService {
    fun getTasksByProject(projectId: Long): List<TaskDto>
    fun getTaskById(id: Long): TaskDto?
    fun createTask(taskDto: TaskCreateDto): TaskDto
    fun updateTask(id: Long, taskDto: TaskUpdateDto): TaskDto?
    fun deleteTask(id: Long): Boolean
    fun getTasksForDropdown(projectId: Long): List<String>
}
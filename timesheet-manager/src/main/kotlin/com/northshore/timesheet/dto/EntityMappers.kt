package com.northshore.timesheet.dto

import com.northshore.timesheet.entity.Project
import com.northshore.timesheet.entity.Task
import com.northshore.timesheet.entity.TimesheetEntry
import com.northshore.timesheet.entity.User


// Project mappers
fun Project.toDto() = ProjectDto(
    id = id,
    name = name,
    description = description,
    startDate = startDate,
    endDate = endDate,
    projectManagerId = projectManager?.id,
    taskCount = tasks.size,
    createdAt = createdAt
)

fun ProjectCreateDto.toEntity(projectManager: User? = null) = Project(
    name = name,
    description = description,
    startDate = startDate,
    endDate = endDate,
    projectManager = projectManager
)

// Task mappers
fun Task.toDto() = TaskDto(
    id = id,
    projectId = project?.id ?: throw IllegalStateException("Task must have a project"),
    name = name,
    description = description,
    estimatedHours = estimatedHours,
    createdAt = createdAt
)

fun TaskCreateDto.toEntity(project: Project) = Task(
    name = name,
    description = description,
    estimatedHours = estimatedHours,
    project = project
)

// TimesheetEntry mappers
fun TimesheetEntry.toDto() = TimesheetEntryDto(
    id = id,
    taskId = task?.id ?: throw IllegalStateException("Entry must have a task"),
    employeeName = employeeName,
    hoursWorked = hoursWorked,
    workDate = workDate,
    notes = notes,
    submittedAt = submittedAt
)

fun TimesheetEntryCreateDto.toEntity(task: Task) = TimesheetEntry(
    employeeName = employeeName,
    hoursWorked = hoursWorked,
    workDate = workDate,
    notes = notes,
    task = task
)
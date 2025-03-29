package com.northshore.timesheet.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kotlinx.datetime.LocalDateTime

data class TaskDto(
    val id: Long? = null,
    val projectId: Long,
    val name: String,
    val description: String? = null,
    val estimatedHours: Double? = null,
    val createdAt: LocalDateTime? = null
)

data class TaskCreateDto(
    @field:NotNull(message = "Project ID is required")
    val projectId: Long,

    @field:NotBlank(message = "Task name is required")
    val name: String,

    val description: String? = null,

    @field:Positive(message = "Estimated hours must be positive")
    val estimatedHours: Double? = null
)

data class TaskUpdateDto(
    @field:NotBlank(message = "Task name is required")
    val name: String,

    val description: String? = null,

    @field:Positive(message = "Estimated hours must be positive")
    val estimatedHours: Double? = null
)
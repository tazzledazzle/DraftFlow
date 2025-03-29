package com.northshore.timesheet.dto


import jakarta.validation.constraints.NotBlank
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class ProjectDto(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val projectManagerId: Long? = null,
    val taskCount: Int = 0,
    val createdAt: LocalDateTime? = null
)

data class ProjectCreateDto(
    @field:NotBlank(message = "Project name is required")
    val name: String,
    val description: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val projectManagerId: Long? = null
)

data class ProjectUpdateDto(
    @field:NotBlank(message = "Project name is required")
    val name: String,
    val description: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val projectManagerId: Long? = null
)
package com.northshore.timesheet.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime


data class TimesheetEntryDto(
    val id: Long? = null,
    val taskId: Long,
    val employeeName: String,
    val hoursWorked: Double,
    val workDate: LocalDate,
    val notes: String? = null,
    val submittedAt: LocalDateTime? = null
)

data class TimesheetEntryCreateDto(
    @field:NotNull(message = "Task ID is required")
    val taskId: Long,

    @field:NotBlank(message = "Employee name is required")
    val employeeName: String,

    @field:Positive(message = "Hours worked must be positive")
    val hoursWorked: Double,

    @field:NotNull(message = "Work date is required")
    val workDate: LocalDate,

    val notes: String? = null
)

data class TaskHoursDto(
    val taskId: Long,
    val taskName: String,
    val totalHours: Double
)
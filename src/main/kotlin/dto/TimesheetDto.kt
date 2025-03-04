package com.northshore.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import services.toDto
import java.time.LocalDate
import java.time.LocalDateTime

data class TimesheetEntryDto(
    val id: Long? = null,
    @field:NotNull(message = "Task ID is required")
    val taskId: Long,
    @field:NotBlank(message = "User name is required")
    val username: String,
    @field:Positive(message = "Hours must be positive")
    val hoursWorked: Double,

    val notes: String? = null,
    @field:NotNull(message = "workDate is required")
    @field:JsonFormat(pattern = "yyyy-MM-dd--HH:mm:ss")
    val workDate: LocalDate,

    @field:JsonFormat(pattern = "yyyy-MM-dd--HH:mm:ss")
    val submittedAt: LocalDateTime? = null,
)

data class TimesheetSubmitDto (
    @field:NotNull(message = "Task ID is required")
    val taskId: Long,
    @field:NotBlank(message = "User name is required")
    val username: String,
    @field:Positive(message = "Hours must be positive")
    val hoursWorked: Double,
    val notes: String? = null,
    @field:NotNull(message = "workDate is required")
    @field:JsonFormat(pattern = "yyyy-MM-dd--HH:mm:ss")
    val workDate: LocalDate,

    @field:JsonFormat(pattern = "yyyy-MM-dd--HH:mm:ss")
    val submittedAt: LocalDateTime? = null,
)
data class TimesheetBatchSubmitDto (
    @field:NotNull(message = "Task ID is required")
    val taskId: Long,
    @field:NotBlank(message = "User name is required")
    val username: String,
    @field:Positive(message = "Hours must be positive")
    val hoursWorked: Double,
    val notes: String? = null,
    @field:NotNull(message = "workDate is required")
    @field:JsonFormat(pattern = "yyyy-MM-dd--HH:mm:ss")
    val workDate: LocalDateTime,

    @field:JsonFormat(pattern = "yyyy-MM-dd--HH:mm:ss")
    val submittedAt: LocalDateTime? = null,
)

data class WeeklyTimesheetDto (
    @field:NotNull(message = "Task ID is required")
    val taskId: Long,
    @field:NotBlank(message = "User name is required")
    val username: String,
    @field:Positive(message = "Hours must be positive")
    val hoursWorked: Double,
    val notes: String? = null,
    @field:NotNull(message = "workDate is required")
    @field:JsonFormat(pattern = "yyyy-MM-dd--HH:mm:ss")
    val workDate: LocalDateTime,

    @field:JsonFormat(pattern = "yyyy-MM-dd--HH:mm:ss")
    val submittedAt: LocalDateTime? = null,

    val employeeName: String? = username,
    val weekStartDate: LocalDate,
    val weekEndDate: LocalDate,
    val totalHours: Double?,
    val entries: List<WeeklyTimesheetDto> = emptyList()
)

data class TaskHoursDto(
    val taskId: Long,
    val taskName: String,
    val totalHours: Double
)

data class EmployeeHoursDto(
    val employeeName: String,
    val totalHours: Double
)
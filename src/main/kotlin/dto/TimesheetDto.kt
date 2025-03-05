package com.northshore.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.Valid
import jakarta.validation.constraints.*
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
    var entries: List<WeeklyTimesheetDto> = emptyList()
)

data class TaskHoursDto(
    val taskId: Long,
    val taskName: String,
    val totalHours: Double
)
data class TimesheetEntryUpdateRequest(
    /**
     * The task ID associated with this timesheet entry.
     * Can be updated to reassign work to a different task.
     */
    @field:NotNull(message = "Task ID is required")
    val taskId: Long? = null,

    /**
     * The number of hours worked.
     * Must be a positive value.
     */
    @field:NotNull(message = "Hours worked is required")
    @field:Min(value = 0, message = "Hours worked must be greater than zero")
    val hoursWorked: Double? = null,

    /**
     * The date when the work was performed.
     * Cannot be in the future.
     */
    @field:NotNull(message = "Work date is required")
    @field:PastOrPresent(message = "Work date cannot be in the future")
    val workDate: LocalDate? = null,

    /**
     * Optional notes about the work performed.
     */
    val notes: String? = null
)


/**
 * Data Transfer Object representing a complete timesheet with its entries.
 * This provides a consolidated view of timesheet data for a specific employee,
 * project, and time period.
 */
data class TimesheetDTO(
    /**
     * Unique identifier for the timesheet.
     * This will be null for new timesheets.
     */
    val id: Long? = null,

    /**
     * ID of the project this timesheet is associated with.
     */
    @field:NotNull(message = "Project ID is required")
    val projectId: Long,

    /**
     * Name of the project (for display purposes).
     */
    val projectName: String? = null,

    /**
     * Name of the employee submitting the timesheet.
     */
    @field:NotBlank(message = "Employee name is required")
    @field:Size(min = 2, max = 100, message = "Employee name must be between 2 and 100 characters")
    val employeeName: String,

    /**
     * The starting date of the timesheet period.
     */
    @field:NotNull(message = "Period start date is required")
    val periodStartDate: LocalDate,

    /**
     * The ending date of the timesheet period.
     */
    @field:NotNull(message = "Period end date is required")
    val periodEndDate: LocalDate,

    /**
     * Status of the timesheet (e.g., DRAFT, SUBMITTED, APPROVED, REJECTED).
     */
    val status: TimesheetStatus = TimesheetStatus.DRAFT,

    /**
     * Comments about the timesheet, such as approval notes or rejection reasons.
     */
    val comments: String? = null,

    /**
     * List of timesheet entries included in this timesheet.
     */
    @field:NotEmpty(message = "Timesheet must contain at least one entry")
    @field:Valid
    val entries: List<TimesheetEntryDto> = emptyList(),

    /**
     * Total hours across all entries in this timesheet.
     */
    val totalHours: Double = 0.0,

    /**
     * When the timesheet was first created.
     */
    val createdAt: LocalDateTime? = null,

    /**
     * When the timesheet was last updated.
     */
    val updatedAt: LocalDateTime? = null,

    /**
     * When the timesheet was submitted for approval.
     */
    val submittedAt: LocalDateTime? = null,

    /**
     * When the timesheet was approved or rejected.
     */
    val processedAt: LocalDateTime? = null,

    /**
     * ID of the user who processed (approved/rejected) the timesheet.
     */
    val processedById: Long? = null,

    /**
     * Name of the user who processed the timesheet (for display purposes).
     */
    val processedByName: String? = null
)

/**
 * Enumeration of possible timesheet statuses.
 */
enum class TimesheetStatus {
    /**
     * Timesheet is being prepared and has not been submitted yet.
     */
    DRAFT,

    /**
     * Timesheet has been submitted and is awaiting approval.
     */
    SUBMITTED,

    /**
     * Timesheet has been approved by a project manager.
     */
    APPROVED,

    /**
     * Timesheet has been rejected by a project manager.
     */
    REJECTED
}
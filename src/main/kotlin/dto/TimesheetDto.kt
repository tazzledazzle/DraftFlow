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
data class TimesheetSubmissionRequest(
    /**
     * The name of the employee submitting the timesheet.
     * This is required and must match the employee name in each entry.
     */
    @field:NotBlank(message = "Employee name is required")
    @field:Size(min = 2, max = 100, message = "Employee name must be between 2 and 100 characters")
    val employeeName: String,

    /**
     * The ID of the project these timesheet entries belong to.
     * This comes from the Excel add-in authentication token.
     */
    @field:NotNull(message = "Project ID is required")
    val projectId: Long,

    /**
     * The start date of the timesheet period.
     * This is typically the beginning of the week or month.
     */
    @field:NotNull(message = "Period start date is required")
    val periodStartDate: LocalDate,

    /**
     * The end date of the timesheet period.
     * This is typically the end of the week or month.
     */
    @field:NotNull(message = "Period end date is required")
    val periodEndDate: LocalDate,

    /**
     * Collection of individual timesheet entries to be submitted.
     * At least one entry is required for a valid submission.
     */
    @field:NotEmpty(message = "At least one timesheet entry is required")
    @field:Valid
    val entries: List<TimesheetEntrySubmission>,

    /**
     * Optional comments from the employee about this submission.
     */
    val comments: String? = null,

    /**
     * Flag indicating if this is a draft submission (to be continued later)
     * or a final submission (ready for approval).
     */
    val isDraft: Boolean = false
) {
    /**
     * Validates consistency between the request and its entries.
     *
     * @return A list of validation errors, or an empty list if valid
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        // Validate that all entries are for the same employee
        entries.forEach { entry ->
            if (entry.employeeName != employeeName) {
                errors.add("All entries must be for the same employee: ${entry.employeeName} != $employeeName")
            }
        }

        // Validate that all entries fall within the period range
        entries.forEach { entry ->
            if (entry.workDate.isBefore(periodStartDate) || entry.workDate.isAfter(periodEndDate)) {
                errors.add("Entry date ${entry.workDate} is outside the timesheet period ($periodStartDate to $periodEndDate)")
            }
        }

        // Validate that there are no duplicate dates/tasks combinations
        val dateTaskPairs = entries.map { Pair(it.workDate, it.taskId) }
        if (dateTaskPairs.size != dateTaskPairs.distinct().size) {
            errors.add("Duplicate entries for the same task and date are not allowed")
        }

        return errors
    }
}

/**
 * Data class representing a single timesheet entry within a batch submission.
 * This is a simplified version of TimesheetEntryDto specifically for submission.
 */
data class TimesheetEntrySubmission(
    /**
     * The ID of the task this entry records time for.
     */
    @field:NotNull(message = "Task ID is required")
    val taskId: Long,

    /**
     * The name of the employee who performed the work.
     * Should match the parent request's employeeName.
     */
    @field:NotBlank(message = "Employee name is required")
    val employeeName: String,

    /**
     * The number of hours worked on this task on this date.
     */
    @field:NotNull(message = "Hours worked is required")
    val hoursWorked: Double,

    /**
     * The date when the work was performed.
     */
    @field:NotNull(message = "Work date is required")
    val workDate: LocalDate,

    /**
     * Optional notes about the work performed.
     */
    val notes: String? = null
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
    val entries: List<TimesheetSubmissionRequest>? = emptyList(),

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

/**
 * Data Transfer Object for timesheet approval or rejection operations.
 * This DTO encapsulates all the information needed when a project manager
 * approves or rejects a submitted timesheet.
 */
data class TimesheetApprovalRequest(
    /**
     * The ID of the timesheet being approved or rejected.
     */
    @field:NotNull(message = "Timesheet ID is required")
    val timesheetId: Long,

    /**
     * Whether the timesheet is being approved (true) or rejected (false).
     */
    @field:NotNull(message = "Approval decision is required")
    val approved: Boolean,

    /**
     * Comments explaining the approval or rejection decision.
     * Optional for approvals, but recommended for rejections to explain why.
     */
    @field:Size(max = 500, message = "Comments cannot exceed 500 characters")
    val comments: String? = null,

    /**
     * The ID of the user (project manager) making the approval decision.
     * This is typically set from the authenticated user on the server side,
     * but is included here for audit trail purposes.
     */
    val approverId: Long? = null,

    /**
     * The timestamp when the approval decision was made.
     * Typically set by the server, but included here for completeness.
     */
    val approvalTimestamp: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Validates the request based on business rules.
     *
     * @return A list of validation errors, or an empty list if valid
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        // If rejecting, comments should be provided to explain why
        if (!approved && comments.isNullOrBlank()) {
            errors.add("Comments are required when rejecting a timesheet")
        }

        return errors
    }

    /**
     * Creates an audit message describing this approval action.
     *
     * @param approverName The name of the person making the approval decision
     * @return A formatted audit message
     */
    fun createAuditMessage(approverName: String): String {
        val action = if (approved) "approved" else "rejected"
        val commentSection = if (!comments.isNullOrBlank()) " with comment: \"$comments\"" else ""

        return "Timesheet #$timesheetId was $action by $approverName on " +
                "${approvalTimestamp.toLocalDate()}$commentSection"
    }
}
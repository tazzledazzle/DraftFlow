package dto

import com.northshore.dto.TimesheetEntryDto
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import java.time.LocalDate
import java.time.LocalDateTime

data class TaskDto (
    var id: Long? = null,
    var projectId: Long = 0L,
    var name: String = "",
    var description: String = "",
    var estimatedHours: Double = 0.0,
    var createdAt: String = ""
)

data class TaskCreateDto(
    var id: Long? = null,

    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotNull(message = "Project ID is required")
    val projectId: Long,

    val description: String,

    @field:PositiveOrZero(message = "Estimated hours must be 0 or greater")
    val estimatedHours: Double? = null
)

data class TaskUpdateDto(
    var id: Long? = null,

    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotNull(message = "Project ID is required")
    val projectId: Long,

    val description: String,

    @field:PositiveOrZero(message = "Estimated hours must be 0 or greater")
    val estimatedHours: Double? = null
)

data class TaskHoursDto(
    val id: Long,

    @field:NotNull(message = "Project ID is required")
    val projectId: Long,

    val name: String,
    @field:PositiveOrZero(message = "Estimated hours must be 0 or greater")
    val estimatedHours: Double,
    @field:PositiveOrZero(message = "Hours worked must be 0 or greater")
    val hoursWorked: Double
)


/**
 * Data Transfer Object representing task progress information.
 * This DTO provides a comprehensive view of a task's progress metrics including
 * time estimates, actual hours spent, and completion percentage.
 */
data class TaskProgressDto(
    /**
     * The unique identifier of the task.
     */
    @field:NotNull(message = "Task ID is required")
    val id: Long,

    /**
     * The name of the task.
     */
    @field:NotBlank(message = "Task name is required")
    val name: String,

    /**
     * The project ID this task belongs to.
     */
    @field:NotNull(message = "Project ID is required")
    val projectId: Long,

    /**
     * The name of the project (for display purposes).
     */
    val projectName: String? = null,

    /**
     * The estimated number of hours to complete the task.
     * A value of 0 indicates no estimate was provided.
     */
    val estimatedHours: Double = 0.0,

    /**
     * The actual number of hours recorded on the task so far.
     * This is calculated from all timesheet entries for this task.
     */
    val actualHours: Double = 0.0,

    /**
     * The percentage of the task that has been completed.
     * This is a value between 0 and 100.
     */
    @field:Min(value = 0, message = "Progress cannot be negative")
    @field:Max(value = 100, message = "Progress cannot exceed 100%")
    var progress: Double = 0.0,

    /**
     * The current status of the task.
     */
    val status: TaskStatus = TaskStatus.NOT_STARTED,

    /**
     * The start date of the task, if it has started.
     */
    val startDate: LocalDate? = null,

    /**
     * The due date of the task, if applicable.
     */
    val dueDate: LocalDate? = null,

    /**
     * The completion date of the task, if it has been completed.
     */
    val completionDate: LocalDate? = null,

    /**
     * The number of employees who have logged time on this task.
     */
    val contributorCount: Int = 0,

    /**
     * Whether the task is overdue based on its due date.
     */
    val isOverdue: Boolean = false,

    /**
     * Whether the task is at risk of not meeting its deadline.
     * This is determined by comparing progress against remaining time.
     */
    val isAtRisk: Boolean = false,

    /**
     * The percentage of budget consumed, calculated as
     * (actualHours / estimatedHours) * 100.
     * Values over 100 indicate the task has exceeded its time budget.
     */
    val budgetConsumedPercentage: Double = 0.0,

    /**
     * An efficiency metric calculated as
     * (progress / budgetConsumedPercentage) * 100.
     * A value of 100 indicates perfect alignment of progress with time spent.
     * Higher values indicate more efficient progress than expected.
     * Lower values indicate less progress than expected for the time spent.
     */
    val efficiencyRatio: Double = 0.0,

    /**
     * When the task was created.
     */
    val createdAt: LocalDateTime? = null,

    /**
     * When the task was last updated.
     */
    val lastUpdatedAt: LocalDateTime? = null,

    /**
     * List of recent timesheet entries for this task.
     * Typically limited to the most recent entries for UI display.
     */
    val recentEntries: List<TimesheetEntryDto> = emptyList()
)

/**
 * Enumeration of possible task statuses.
 */
enum class TaskStatus {
    /**
     * Task has been created but work has not begun.
     */
    NOT_STARTED,

    /**
     * Task is actively being worked on.
     */
    IN_PROGRESS,

    /**
     * Task has been temporarily stopped.
     */
    ON_HOLD,

    /**
     * Task has been completed.
     */
    COMPLETED,

    /**
     * Task has been canceled before completion.
     */
    CANCELED
}
package controllers


/*
        Endpoint	Method	Description	Request	Response
        /api/timesheets	POST	Submit timesheet entry	TimesheetEntryDto	TimesheetEntryDto
        /api/timesheets/project/{projectId}	GET	Get timesheet entries for project	-	Array of TimesheetEntryDto
        /api/timesheets/task/{taskId}	GET	Get timesheet entries for task	-	Array of TimesheetEntryDto

*/

import com.northshore.dto.TimesheetEntryDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import services.TimesheetEntryService
import java.time.LocalDate
import java.util.*

/**
 * REST controller for the Timesheet Service
 * Provides API endpoints for time entry and timesheet management
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Timesheet API", description = "API endpoints for timesheet management")
class TimesheetController @Autowired constructor(private val timesheetService: TimesheetEntryService) {
    /**
     * Record a clock-in event for a worker
     */
    @PostMapping("/time-entries/clock-in")
    @Operation(
        summary = "Clock in a worker",
        description = "Records the start of a work period for an employee at a specific project",
        responses = [ApiResponse(
            responseCode = "201",
            description = "Clock-in recorded successfully",
            content = Content(schema = Schema(implementation = TimeEntryDTO::class))
        ), ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ), ApiResponse(responseCode = "409", description = "Worker already has an active time entry")]
    )
    fun clockIn(
        @Valid @RequestBody request: ClockInRequest
    ): ResponseEntity<TimeEntryDTO?> {
        log.info("Received clock-in request for employee: {}", request.getEmployeeId())
        val timeEntry: TimeEntryDTO? = timesheetService.clockIn(request)
        return ResponseEntity<TimeEntryDTO?>(timeEntry, HttpStatus.CREATED)
    }

    /**
     * Record a clock-out event for an existing time entry
     */
    @PostMapping("/time-entries/{timeEntryId}/clock-out")
    @Operation(
        summary = "Clock out a worker",
        description = "Records the end of a work period for an existing time entry",
        responses = [ApiResponse(
            responseCode = "200",
            description = "Clock-out recorded successfully",
            content = Content(schema = Schema(implementation = TimeEntryDTO::class))
        ), ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ), ApiResponse(responseCode = "404", description = "Time entry not found"), ApiResponse(
            responseCode = "409",
            description = "Time entry is not active"
        )]
    )
    fun clockOut(
        @Parameter(description = "ID of the time entry to update") @PathVariable timeEntryId: UUID?,
        @Valid @RequestBody request: ClockOutRequest?
    ): ResponseEntity<TimeEntryDTO?> {
        log.info("Received clock-out request for time entry: {}", timeEntryId)
        val timeEntry: TimeEntryDTO? = timesheetService.clockOut(timeEntryId, request)
        return ResponseEntity.ok<TimeEntryDTO?>(timeEntry)
    }

    /**
     * Get details of a specific time entry
     */
    @GetMapping("/time-entries/{timeEntryId}")
    @Operation(
        summary = "Get time entry details",
        description = "Retrieves the details of a specific time entry",
        responses = [ApiResponse(
            responseCode = "200",
            description = "Time entry found",
            content = Content(schema = Schema(implementation = TimeEntryDTO::class))
        ), ApiResponse(responseCode = "404", description = "Time entry not found")]
    )
    fun getTimeEntry(
        @Parameter(description = "ID of the time entry to retrieve") @PathVariable timeEntryId: UUID?
    ): ResponseEntity<TimesheetEntryDto?> {
        log.info("Retrieving time entry: {}", timeEntryId)
        val timeEntry: TimesheetEntryDto? = timesheetService.getTimeEntryById(timeEntryId)
        return ResponseEntity.ok<TimesheetEntryDto?>(timeEntry)
    }

    /**
     * Update a specific time entry
     */
    @PutMapping("/time-entries/{timeEntryId}")
    @Operation(
        summary = "Update time entry",
        description = "Updates the details of a specific time entry",
        responses = [ApiResponse(
            responseCode = "200",
            description = "Time entry updated successfully",
            content = Content(schema = Schema(implementation = TimeEntryDTO::class))
        ), ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ), ApiResponse(responseCode = "404", description = "Time entry not found"), ApiResponse(
            responseCode = "409",
            description = "Cannot update time entry in an approved timesheet"
        )]
    )
    fun updateTimeEntry(
        @Parameter(description = "ID of the time entry to update") @PathVariable timeEntryId: UUID?,
        @Valid @RequestBody request: TimeEntryUpdateRequest?
    ): ResponseEntity<TimeEntryDTO?> {
        log.info("Updating time entry: {}", timeEntryId)
        val timeEntry: TimeEntryDTO? = timesheetService.updateTimeEntry(timeEntryId, request)
        return ResponseEntity.ok<TimeEntryDTO?>(timeEntry)
    }

    /**
     * Delete a specific time entry
     */
    @DeleteMapping("/time-entries/{timeEntryId}")
    @Operation(
        summary = "Delete time entry",
        description = "Deletes a specific time entry",
        responses = [ApiResponse(responseCode = "204", description = "Time entry deleted successfully"), ApiResponse(
            responseCode = "404",
            description = "Time entry not found"
        ), ApiResponse(responseCode = "409", description = "Cannot delete time entry in an approved timesheet")]
    )
    fun deleteTimeEntry(
        @Parameter(description = "ID of the time entry to delete") @PathVariable timeEntryId: UUID?
    ): ResponseEntity<Void?> {
        log.info("Deleting time entry: {}", timeEntryId)
        timesheetService.deleteTimeEntry(timeEntryId)
        return ResponseEntity.noContent().build<Void?>()
    }

    /**
     * Get a specific timesheet
     */
    @GetMapping("/timesheets/{timesheetId}")
    @Operation(
        summary = "Get timesheet details",
        description = "Retrieves the details of a specific timesheet including its time entries",
        responses = [ApiResponse(
            responseCode = "200",
            description = "Timesheet found",
            content = Content(schema = Schema(implementation = TimesheetDTO::class))
        ), ApiResponse(responseCode = "404", description = "Timesheet not found")]
    )
    fun getTimesheet(
        @Parameter(description = "ID of the timesheet to retrieve") @PathVariable timesheetId: UUID?
    ): ResponseEntity<TimesheetDTO?> {
        log.info("Retrieving timesheet: {}", timesheetId)
        val timesheet: TimesheetDTO? = timesheetService.getTimesheet(timesheetId)
        return ResponseEntity.ok<TimesheetDTO?>(timesheet)
    }

    /**
     * Get timesheets for an employee within a date range
     */
    @GetMapping("/employees/{employeeId}/timesheets")
    @Operation(
        summary = "Get employee timesheets",
        description = "Retrieves timesheets for a specific employee within a date range",
        responses = [ApiResponse(
            responseCode = "200",
            description = "Timesheets retrieved successfully",
            content = Content(schema = Schema(implementation = TimesheetDTO::class))
        ), ApiResponse(responseCode = "400", description = "Invalid request parameters")]
    )
    fun getEmployeeTimesheets(
        @Parameter(description = "ID of the employee") @PathVariable employeeId: UUID?,
        @Parameter(description = "Start date (inclusive)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @Parameter(description = "End date (inclusive)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ): ResponseEntity<MutableList<TimesheetDTO?>?> {
        log.info(
            "Retrieving timesheets for employee: {} between dates: {} and {}",
            employeeId, startDate, endDate
        )
        val timesheets: MutableList<TimesheetDTO?>? = timesheetService
            .getTimesheetsByEmployeeAndDateRange(employeeId, startDate, endDate)
        return ResponseEntity.ok<MutableList<TimesheetDTO?>?>(timesheets)
    }

    /**
     * Submit a timesheet for approval
     */
    @PostMapping("/timesheets/{timesheetId}/submit")
    @Operation(
        summary = "Submit timesheet for approval",
        description = "Submits a timesheet for supervisor approval",
        responses = [ApiResponse(
            responseCode = "200",
            description = "Timesheet submitted successfully",
            content = Content(schema = Schema(implementation = TimesheetDTO::class))
        ), ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ), ApiResponse(responseCode = "404", description = "Timesheet not found"), ApiResponse(
            responseCode = "409",
            description = "Timesheet is not in a submittable state"
        )]
    )
    fun submitTimesheet(
        @Parameter(description = "ID of the timesheet to submit") @PathVariable timesheetId: UUID?,
        @Valid @RequestBody request: TimesheetSubmissionRequest?
    ): ResponseEntity<TimesheetDTO?> {
        log.info("Submitting timesheet: {}", timesheetId)
        val timesheet: TimesheetDTO? = timesheetService.submitTimesheet(timesheetId, request)
        return ResponseEntity.ok<TimesheetDTO?>(timesheet)
    }

    /**
     * Approve a submitted timesheet
     */
    @PostMapping("/timesheets/{timesheetId}/approve")
    @Operation(
        summary = "Approve timesheet",
        description = "Approves a submitted timesheet",
        responses = [ApiResponse(
            responseCode = "200",
            description = "Timesheet approved successfully",
            content = Content(schema = Schema(implementation = TimesheetDTO::class))
        ), ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ), ApiResponse(responseCode = "404", description = "Timesheet not found"), ApiResponse(
            responseCode = "409",
            description = "Timesheet is not in a submittable state"
        )]
    )
    fun approveTimesheet(
        @Parameter(description = "ID of the timesheet to approve") @PathVariable timesheetId: UUID?,
        @Valid @RequestBody request: TimesheetApprovalRequest?
    ): ResponseEntity<TimesheetDTO?> {
        log.info("Approving timesheet: {}", timesheetId)
        val timesheet: TimesheetDTO? = timesheetService.approveTimesheet(timesheetId, request)
        return ResponseEntity.ok<TimesheetDTO?>(timesheet)
    }

    /**
     * Reject a submitted timesheet
     */
    @PostMapping("/timesheets/{timesheetId}/reject")
    @Operation(
        summary = "Reject timesheet",
        description = "Rejects a submitted timesheet",
        responses = [ApiResponse(
            responseCode = "200",
            description = "Timesheet rejected successfully",
            content = Content(schema = Schema(implementation = TimesheetDTO::class))
        ), ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ), ApiResponse(responseCode = "404", description = "Timesheet not found"), ApiResponse(
            responseCode = "409",
            description = "Timesheet is not in a submittable state"
        )]
    )
    fun rejectTimesheet(
        @Parameter(description = "ID of the timesheet to reject") @PathVariable timesheetId: UUID?,
        @Valid @RequestBody request: TimesheetRejectionRequest?
    ): ResponseEntity<TimesheetDTO?> {
        log.info("Rejecting timesheet: {}", timesheetId)
        val timesheet: TimesheetDTO? = timesheetService.rejectTimesheet(timesheetId, request)
        return ResponseEntity.ok<TimesheetDTO?>(timesheet)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(TimesheetController::class.java)
    }
}
package services

import com.northshore.dto.EmployeeHoursDto
import com.northshore.dto.TimesheetBatchSubmitDto
import com.northshore.dto.TimesheetSubmitDto
import com.northshore.dto.TimesheetEntryDto
import com.northshore.dto.WeeklyTimesheetDto
import com.northshore.exceptions.InvalidDataException
import com.northshore.exceptions.ResourceNotFoundException
import dto.TaskHoursDto
import models.TimesheetEntry
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import repository.ProjectRepository
import repository.TaskRepository
import repository.TimesheetEntryRepository
import java.time.LocalDate
import java.time.LocalDateTime


interface TimesheetEntryService {
    fun submitTimesheetEntry(entryDto: TimesheetSubmitDto): TimesheetEntryDto
    fun submitTimesheetBatch(batchDto: List<TimesheetEntryDto>): List<TimesheetEntryDto>
    fun getTimesheetEntriesByTask(taskId: Long): List<TimesheetEntryDto>
    fun getTimesheetEntriesByProject(projectId: Long): List<TimesheetEntryDto>
    fun getWeeklyTimesheet(employeeName: String, weekStartDate: LocalDateTime): WeeklyTimesheetDto
    fun getEntriesByDateRange(startDate: LocalDate, endDate: LocalDate): List<TimesheetEntryDto>
    fun getEmployeeHoursSummary(projectId: Long): List<EmployeeHoursDto>
}
@Service
class TimesheetEntryServiceImpl(
    private val timesheetEntryRepository: TimesheetEntryRepository,
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository
) : TimesheetEntryService {

    @Transactional
    override fun submitTimesheetEntry(entryDto: TimesheetSubmitDto): TimesheetEntryDto {
        // Validate the task exists
        val task = taskRepository.findById(entryDto.taskId)
            .orElseThrow { ResourceNotFoundException("Task not found with id: ${entryDto.taskId}") }

        // Validate hours worked is positive
        if (entryDto.hoursWorked <= 0) {
            throw InvalidDataException("Hours worked must be greater than zero")
        }

        // Create and save timesheet entry
        val entry = TimesheetEntry(
            taskId = task.id!!,
            employeeName = entryDto.username,
            hoursWorked = entryDto.hoursWorked,
            workDate = entryDto.workDate,
            notes = entryDto.notes,
            submittedAt = LocalDateTime.now()
        )

        val savedEntry = timesheetEntryRepository.save(entry)
        return savedEntry.toDto()
    }

    @Transactional
    override fun submitTimesheetBatch(entriesDto: List<TimesheetEntryDto>): List<TimesheetEntryDto> {
        // Validate all entries in one go to make this operation atomic
        val taskIds = entriesDto.map { it.taskId }.distinct()
        val existingTasks = taskRepository.findAllById(taskIds)
            .associateBy { it.id!! }

        // Check if all referenced tasks exist
        val missingTaskIds = taskIds.filter { !existingTasks.containsKey(it) }
        if (missingTaskIds.isNotEmpty()) {
            throw ResourceNotFoundException("Tasks not found with ids: $missingTaskIds")
        }

        // Validate hours worked for all entries
        entriesDto.forEach { entryDto ->
            if (entryDto.hoursWorked <= 0) {
                throw InvalidDataException("Hours worked must be greater than zero for task: ${entryDto.taskId}")
            }
        }

        // Create and save all entries
        val entries = entriesDto.map { entryDto ->
            TimesheetEntry(
                taskId = entryDto.taskId,
                employeeName = entryDto.username,
                hoursWorked = entryDto.hoursWorked,
                workDate = entryDto.workDate,
                notes = entryDto.notes,
                submittedAt = LocalDateTime.now()
            )
        }

        val savedEntries = timesheetEntryRepository.saveAll(entries)
        return savedEntries.map { it.toDto() }
    }

    override fun getTimesheetEntriesByTask(taskId: Long): List<TimesheetEntryDto> {
        // Check if task exists
        if (!taskRepository.existsById(taskId)) {
            throw ResourceNotFoundException("Task not found with id: $taskId")
        }

        return timesheetEntryRepository.findByTaskId(taskId)
            .map { it.toDto() }
    }

    override fun getTimesheetEntriesByProject(projectId: Long): List<TimesheetEntryDto> {
        // Check if project exists
        if (!projectRepository.existsById(projectId)) {
            throw ResourceNotFoundException("Project not found with id: $projectId")
        }

        return timesheetEntryRepository.findByTaskProjectId(projectId)
            .map { it.toDto() }
    }

    override fun getEntriesByDateRange(startDate: LocalDate, endDate: LocalDate): List<TimesheetEntryDto> {
        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw InvalidDataException("Start date cannot be after end date")
        }

        return timesheetEntryRepository.findByWorkDateBetween(startDate, endDate)
            .map { it.toDto() }
    }

    fun getTaskHoursSummary(projectId: Long): List<TaskHoursDto> {
        // Check if project exists
        if (!projectRepository.existsById(projectId)) {
            throw ResourceNotFoundException("Project not found with id: $projectId")
        }

        return timesheetEntryRepository.getTaskHoursByProject(projectId)
    }

    override fun getWeeklyTimesheet(employeeName: String, weekStartDate: LocalDateTime): WeeklyTimesheetDto {
        // Calculate the end date of the week
        val weekEndDate = weekStartDate.plusDays(6)

        // Fetch all timesheet entries for the employee within the week
        val entries = timesheetEntryRepository.findByEmployeeNameAndWorkDateBetween(employeeName, weekStartDate.toLocalDate(), weekEndDate.toLocalDate())

        // Calculate total hours worked for the week
        val totalHours = entries.sumOf { it.hoursWorked }

        return WeeklyTimesheetDto(
            employeeName = employeeName,
            weekStartDate = weekStartDate,
            weekEndDate = weekEndDate,
            totalHours = totalHours,
            entries = entries.map { it.toDto() }
        )

    }
   override fun getEmployeeHoursSummary(projectId: Long): List<EmployeeHoursDto> {
        // Check if project exists
        if (!projectRepository.existsById(projectId)) {
            throw ResourceNotFoundException("Project not found with id: $projectId")
        }

        // Fetch all timesheet entries for the project
        val entries = timesheetEntryRepository.findByTaskProjectId(projectId)

        // Group by employee name and calculate total hours
        return entries.groupBy { it.employeeName }
            .map { (employeeName, entries) ->
                EmployeeHoursDto(
                    employeeName = employeeName,
                    totalHours = entries.sumOf { it.hoursWorked }
                )
            }
            .sortedByDescending { it.totalHours }
    }

    @Transactional
    override fun deleteEntry(id: Long): Boolean {
        return if (timesheetEntryRepository.existsById(id)) {
            timesheetEntryRepository.deleteById(id)
            true
        } else {
            false
        }
    }


}

// Extension function to convert entity to DTO
fun TimesheetEntry.toDto(): TimesheetEntryDto = TimesheetEntryDto(
    id = this.id,
    taskId = this.task.id!!,
    employeeName = this.employeeName,
    hoursWorked = this.hoursWorked,
    workDate = this.workDate,
    notes = this.notes,
    submittedAt = this.submittedAt
)


//
//import com.northshore.services.ProjectService
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.kafka.core.KafkaTemplate
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Transactional
//import repository.TimesheetEntryRepository
//import java.time.Duration
//import java.time.LocalDateTime
//import java.util.*
//
//@Service
//class TimesheetService @Autowired constructor(
//    timesheetRepository: TimesheetEntryRepository?,
//    timeEntryRepository: TimeEntryRepository,
//    projectService: ProjectService,
//    employeeService: UserService,
//    locationValidator: LocationValidationService,
//    kafkaTemplate: KafkaTemplate<String?, TimesheetEvent?>
//) {
//    private val timesheetRepository: TimesheetRepository?
//    private val timeEntryRepository: TimeEntryRepository
//    private val projectService: ProjectService
//    private val employeeService: EmployeeService
//    private val payRateService: PayRateService
//    private val locationValidator: LocationValidationService
//    private val kafkaTemplate: KafkaTemplate<String?, TimesheetEvent?>
//
//    init {
//        this.timesheetRepository = timesheetRepository
//        this.timeEntryRepository = timeEntryRepository
//        this.projectService = projectService
//        this.employeeService = employeeService
//        this.payRateService = payRateService
//        this.locationValidator = locationValidator
//        this.kafkaTemplate = kafkaTemplate
//    }
//
//    /**
//     * Records a clock-in event for an employee at a specific project
//     *
//     * @param employeeId The ID of the employee clocking in
//     * @param projectId The ID of the project where work is being performed
//     * @param taskCode The specific task being performed (optional)
//     * @param latitude The GPS latitude of the clock-in location
//     * @param longitude The GPS longitude of the clock-in location
//     * @param notes Any additional notes for this clock-in
//     * @return The created TimeEntry
//     */
//    @Transactional
//    fun clockIn(
//        employeeId: UUID?,
//        projectId: UUID?,
//        taskCode: String?,
//        latitude: Double?,
//        longitude: Double?,
//        notes: String?
//    ): TimeEntry? {
//        // Verify employee exists and is active
//
//        if (!employeeService.isEmployeeActiveAndEligible(employeeId)) {
//            throw ValidationException("Employee is not active or eligible to work")
//        }
//
//
//        // Verify project exists and is active
//        if (!projectService.isProjectActive(projectId)) {
//            throw ValidationException("Project is not active")
//        }
//
//
//        // Validate location is within the project site boundaries
//        if (latitude != null && longitude != null) {
//            if (!locationValidator.isLocationWithinProjectBoundary(projectId, latitude, longitude)) {
//                throw ValidationException("Location is outside project boundaries")
//            }
//        }
//
//
//        // Check if employee already has an open time entry
//        val openEntry: Optional<TimeEntry?> = timeEntryRepository.findOpenTimeEntryForEmployee(employeeId)
//        if (openEntry.isPresent()) {
//            throw ValidationException("Employee already has an open time entry. Please clock out first.")
//        }
//
//
//        // Create new time entry
//        val timeEntry: TimeEntry = TimeEntry()
//        timeEntry.setEmployeeId(employeeId)
//        timeEntry.setProjectId(projectId)
//        timeEntry.setTaskCode(taskCode)
//        timeEntry.setClockInTime(LocalDateTime.now())
//        timeEntry.setClockInLatitude(latitude)
//        timeEntry.setClockInLongitude(longitude)
//        timeEntry.setNotes(notes)
//        timeEntry.setStatus("ACTIVE")
//
//
//        // Save the time entry
//        val savedEntry: TimeEntry? = timeEntryRepository.save(timeEntry)
//
//
//        // Publish clock-in event
//        kafkaTemplate.send("timesheet-events", TimesheetEvent("CLOCK_IN", savedEntry))
//
//        return savedEntry
//    }
//
//    /**
//     * Records a clock-out event for an employee
//     *
//     * @param timeEntryId The ID of the time entry to close
//     * @param latitude The GPS latitude of the clock-out location
//     * @param longitude The GPS longitude of the clock-out location
//     * @param notes Any additional notes for this clock-out
//     * @return The updated TimeEntry
//     */
//    @Transactional
//    fun clockOut(
//        timeEntryId: UUID?,
//        latitude: Double?,
//        longitude: Double?,
//        notes: String?
//    ): TimeEntry? {
//        // Find the time entry
//
//        val timeEntry: TimeEntry = timeEntryRepository.findById(timeEntryId)
//            .orElseThrow({ ValidationException("Time entry not found") })
//
//
//        // Verify time entry is still open
//        if ("ACTIVE" != timeEntry.getStatus()) {
//            throw ValidationException("Time entry is not active")
//        }
//
//
//        // Record clock-out time and location
//        val clockOutTime = LocalDateTime.now()
//        timeEntry.setClockOutTime(clockOutTime)
//        timeEntry.setClockOutLatitude(latitude)
//        timeEntry.setClockOutLongitude(longitude)
//
//
//        // Append notes if provided
//        if (notes != null && !notes.trim { it <= ' ' }.isEmpty()) {
//            timeEntry.setNotes(
//                if (timeEntry.getNotes() != null)
//                    timeEntry.getNotes() + " | Clock-out: " + notes
//                else
//                    "Clock-out: " + notes
//            )
//        }
//
//
//        // Calculate duration
//        val duration = Duration.between(timeEntry.getClockInTime(), clockOutTime)
//        val hours = duration.toHours()
//        val minutes = ((duration.toMinutes() % 60) / 15).toInt() * 15 // Round to nearest 15 minutes
//
//        timeEntry.setDurationHours(hours.toDouble() + (minutes / 60.0))
//        timeEntry.setStatus("COMPLETED")
//
//
//        // Calculate pay rates
//        val regularRate: Double = payRateService.getRegularRate(
//            timeEntry.getEmployeeId(),
//            timeEntry.getProjectId(),
//            timeEntry.getTaskCode()
//        )
//
//        val overtimeRate: Double = payRateService.getOvertimeRate(
//            timeEntry.getEmployeeId(),
//            timeEntry.getProjectId(),
//            timeEntry.getTaskCode()
//        )
//
//        timeEntry.setRegularRate(regularRate)
//        timeEntry.setOvertimeRate(overtimeRate)
//
//
//        // Save the updated time entry
//        val savedEntry: TimeEntry? = timeEntryRepository.save(timeEntry)
//
//
//        // Publish clock-out event
//        kafkaTemplate.send("timesheet-events", TimesheetEvent("CLOCK_OUT", savedEntry))
//
//        return savedEntry
//    }
//
//    /**
//     * Submits a weekly timesheet for approval
//     *
//     * @param request The timesheet submission request
//     * @return The created Timesheet
//     */
//    @Transactional
//    fun submitTimesheet(request: TimesheetSubmissionRequest?): Timesheet? {
//        // Implementation details for submitting a weekly timesheet
//        // This would include validating all time entries, calculating totals,
//        // determining regular vs. overtime hours, and initiating approval workflow
//
//        // For brevity, implementation details are omitted
//
//        // Return a placeholder
//
//        return Timesheet()
//    }
//
//    /**
//     * Approves a timesheet, marking it ready for payroll processing
//     *
//     * @param timesheetId The ID of the timesheet to approve
//     * @param approverId The ID of the supervisor approving the timesheet
//     * @param notes Approval notes or comments
//     * @return The updated Timesheet
//     */
//    @Transactional
//    fun approveTimesheet(timesheetId: UUID?, approverId: UUID?, notes: String?): Timesheet? {
//        // Implementation details for timesheet approval process
//        // This would include permission checks, digital signature capture,
//        // and workflow state transitions
//
//        // For brevity, implementation details are omitted
//
//        // Return a placeholder
//
//        return Timesheet()
//    } // Additional methods for timesheet management would be included here
//}
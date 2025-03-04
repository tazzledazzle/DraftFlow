package services


import com.construction.timesheet.dto.TimesheetSubmissionRequest
import com.construction.timesheet.events.TimesheetEvent
import com.construction.timesheet.exception.ValidationException
import com.construction.timesheet.model.TimeEntry
import com.construction.timesheet.model.Timesheet
import com.construction.timesheet.repository.TimeEntryRepository
import com.construction.timesheet.repository.TimesheetRepository
import com.northshore.services.ProjectService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
class TimesheetService @Autowired constructor(
    timesheetRepository: TimesheetRepository?,
    timeEntryRepository: TimeEntryRepository,
    projectService: ProjectService,
    employeeService: EmployeeService,
    payRateService: PayRateService,
    locationValidator: LocationValidationService,
    kafkaTemplate: KafkaTemplate<String?, TimesheetEvent?>
) {
    private val timesheetRepository: TimesheetRepository?
    private val timeEntryRepository: TimeEntryRepository
    private val projectService: ProjectService
    private val employeeService: EmployeeService
    private val payRateService: PayRateService
    private val locationValidator: LocationValidationService
    private val kafkaTemplate: KafkaTemplate<String?, TimesheetEvent?>

    init {
        this.timesheetRepository = timesheetRepository
        this.timeEntryRepository = timeEntryRepository
        this.projectService = projectService
        this.employeeService = employeeService
        this.payRateService = payRateService
        this.locationValidator = locationValidator
        this.kafkaTemplate = kafkaTemplate
    }

    /**
     * Records a clock-in event for an employee at a specific project
     *
     * @param employeeId The ID of the employee clocking in
     * @param projectId The ID of the project where work is being performed
     * @param taskCode The specific task being performed (optional)
     * @param latitude The GPS latitude of the clock-in location
     * @param longitude The GPS longitude of the clock-in location
     * @param notes Any additional notes for this clock-in
     * @return The created TimeEntry
     */
    @Transactional
    fun clockIn(
        employeeId: UUID?,
        projectId: UUID?,
        taskCode: String?,
        latitude: Double?,
        longitude: Double?,
        notes: String?
    ): TimeEntry? {
        // Verify employee exists and is active

        if (!employeeService.isEmployeeActiveAndEligible(employeeId)) {
            throw ValidationException("Employee is not active or eligible to work")
        }


        // Verify project exists and is active
        if (!projectService.isProjectActive(projectId)) {
            throw ValidationException("Project is not active")
        }


        // Validate location is within the project site boundaries
        if (latitude != null && longitude != null) {
            if (!locationValidator.isLocationWithinProjectBoundary(projectId, latitude, longitude)) {
                throw ValidationException("Location is outside project boundaries")
            }
        }


        // Check if employee already has an open time entry
        val openEntry: Optional<TimeEntry?> = timeEntryRepository.findOpenTimeEntryForEmployee(employeeId)
        if (openEntry.isPresent()) {
            throw ValidationException("Employee already has an open time entry. Please clock out first.")
        }


        // Create new time entry
        val timeEntry: TimeEntry = TimeEntry()
        timeEntry.setEmployeeId(employeeId)
        timeEntry.setProjectId(projectId)
        timeEntry.setTaskCode(taskCode)
        timeEntry.setClockInTime(LocalDateTime.now())
        timeEntry.setClockInLatitude(latitude)
        timeEntry.setClockInLongitude(longitude)
        timeEntry.setNotes(notes)
        timeEntry.setStatus("ACTIVE")


        // Save the time entry
        val savedEntry: TimeEntry? = timeEntryRepository.save(timeEntry)


        // Publish clock-in event
        kafkaTemplate.send("timesheet-events", TimesheetEvent("CLOCK_IN", savedEntry))

        return savedEntry
    }

    /**
     * Records a clock-out event for an employee
     *
     * @param timeEntryId The ID of the time entry to close
     * @param latitude The GPS latitude of the clock-out location
     * @param longitude The GPS longitude of the clock-out location
     * @param notes Any additional notes for this clock-out
     * @return The updated TimeEntry
     */
    @Transactional
    fun clockOut(
        timeEntryId: UUID?,
        latitude: Double?,
        longitude: Double?,
        notes: String?
    ): TimeEntry? {
        // Find the time entry

        val timeEntry: TimeEntry = timeEntryRepository.findById(timeEntryId)
            .orElseThrow({ ValidationException("Time entry not found") })


        // Verify time entry is still open
        if ("ACTIVE" != timeEntry.getStatus()) {
            throw ValidationException("Time entry is not active")
        }


        // Record clock-out time and location
        val clockOutTime = LocalDateTime.now()
        timeEntry.setClockOutTime(clockOutTime)
        timeEntry.setClockOutLatitude(latitude)
        timeEntry.setClockOutLongitude(longitude)


        // Append notes if provided
        if (notes != null && !notes.trim { it <= ' ' }.isEmpty()) {
            timeEntry.setNotes(
                if (timeEntry.getNotes() != null)
                    timeEntry.getNotes() + " | Clock-out: " + notes
                else
                    "Clock-out: " + notes
            )
        }


        // Calculate duration
        val duration = Duration.between(timeEntry.getClockInTime(), clockOutTime)
        val hours = duration.toHours()
        val minutes = ((duration.toMinutes() % 60) / 15).toInt() * 15 // Round to nearest 15 minutes

        timeEntry.setDurationHours(hours.toDouble() + (minutes / 60.0))
        timeEntry.setStatus("COMPLETED")


        // Calculate pay rates
        val regularRate: Double = payRateService.getRegularRate(
            timeEntry.getEmployeeId(),
            timeEntry.getProjectId(),
            timeEntry.getTaskCode()
        )

        val overtimeRate: Double = payRateService.getOvertimeRate(
            timeEntry.getEmployeeId(),
            timeEntry.getProjectId(),
            timeEntry.getTaskCode()
        )

        timeEntry.setRegularRate(regularRate)
        timeEntry.setOvertimeRate(overtimeRate)


        // Save the updated time entry
        val savedEntry: TimeEntry? = timeEntryRepository.save(timeEntry)


        // Publish clock-out event
        kafkaTemplate.send("timesheet-events", TimesheetEvent("CLOCK_OUT", savedEntry))

        return savedEntry
    }

    /**
     * Submits a weekly timesheet for approval
     *
     * @param request The timesheet submission request
     * @return The created Timesheet
     */
    @Transactional
    fun submitTimesheet(request: TimesheetSubmissionRequest?): Timesheet? {
        // Implementation details for submitting a weekly timesheet
        // This would include validating all time entries, calculating totals,
        // determining regular vs. overtime hours, and initiating approval workflow

        // For brevity, implementation details are omitted

        // Return a placeholder

        return Timesheet()
    }

    /**
     * Approves a timesheet, marking it ready for payroll processing
     *
     * @param timesheetId The ID of the timesheet to approve
     * @param approverId The ID of the supervisor approving the timesheet
     * @param notes Approval notes or comments
     * @return The updated Timesheet
     */
    @Transactional
    fun approveTimesheet(timesheetId: UUID?, approverId: UUID?, notes: String?): Timesheet? {
        // Implementation details for timesheet approval process
        // This would include permission checks, digital signature capture,
        // and workflow state transitions

        // For brevity, implementation details are omitted

        // Return a placeholder

        return Timesheet()
    } // Additional methods for timesheet management would be included here
}
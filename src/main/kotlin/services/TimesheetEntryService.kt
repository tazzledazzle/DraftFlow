package services

import TimesheetEntryRepository
import com.northshore.dto.EmployeeHoursDto
import com.northshore.dto.TaskHoursDto
import com.northshore.dto.TimesheetSubmitDto
import com.northshore.dto.TimesheetEntryDto
import com.northshore.dto.WeeklyTimesheetDto
import com.northshore.exceptions.InvalidDataException
import com.northshore.exceptions.ResourceNotFoundException
import models.TimesheetEntry
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import repository.ProjectRepository
import repository.TaskRepository
import java.time.LocalDate
import java.time.LocalDateTime


interface TimesheetEntryService {
    fun submitTimesheetEntry(entryDto: TimesheetSubmitDto): TimesheetEntryDto
    fun submitTimesheetBatch(batchDto: List<TimesheetEntryDto>): List<TimesheetEntryDto>
    fun getTimesheetEntriesByTask(taskId: Long): List<TimesheetEntryDto>
    fun getTimesheetEntriesByProject(projectId: Long): List<TimesheetEntryDto>
    fun getWeeklyTimesheet(employeeName: String, weekStartDate: LocalDate): WeeklyTimesheetDto
    fun getEntriesByDateRange(startDate: LocalDate, endDate: LocalDate): List<TimesheetEntryDto>
    fun getEmployeeHoursSummary(projectId: Long): List<EmployeeHoursDto>
    fun deleteEntry(id: Long): Boolean
    fun getTimeEntryById(entryId: Long): TimesheetEntryDto
    fun getEntriesByProject(projectId: Long, startDate: LocalDate?, endDate: LocalDate?): List<TimesheetEntryDto>
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

        return timesheetEntryRepository.findByProjectId(projectId)
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

    override fun getWeeklyTimesheet(employeeName: String, weekStartDate: LocalDate): WeeklyTimesheetDto {
        // Calculate the end date of the week
        val weekEndDate = weekStartDate.plusDays(6)

        // Fetch all timesheet entries for the employee within the week
        val entries = timesheetEntryRepository.findByEmployeeNameAndWorkDateBetween(employeeName, weekStartDate, weekEndDate)

        // Calculate total hours worked for the week
        val totalHours = entries.sumOf { it.hoursWorked }
        return WeeklyTimesheetDto(
            username = employeeName,
            hoursWorked = totalHours,
            taskId = entries.firstOrNull()?.taskId ?: 0,
            employeeName = employeeName,
            workDate =  LocalDateTime.now(),
            weekStartDate = weekStartDate,
            weekEndDate = weekEndDate,
            totalHours = totalHours,
            entries = entries
        )

    }


    override fun getEmployeeHoursSummary(projectId: Long): List<EmployeeHoursDto> {
        // Check if project exists
        if (!projectRepository.existsById(projectId)) {
            throw ResourceNotFoundException("Project not found with id: $projectId")
        }

        // Fetch all timesheet entries for the project
        val entries = timesheetEntryRepository.findByProjectId(projectId)

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

    override fun getEntriesByProject(
        projectId: Long,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): List<TimesheetEntryDto> {
        val project = projectRepository.findById(projectId)
            .orElseThrow { ResourceNotFoundException("Project not found with id: $projectId") }
        val entries = timesheetEntryRepository.findByProjectId(projectId)
    }

    override fun getTimeEntryById(entryId: Long): TimesheetEntryDto {
        return timesheetEntryRepository.findByIdOrNull(entryId)
    }

}

// Extension function to convert entity to DTO
fun TimesheetEntry.toDto(): TimesheetEntryDto = TimesheetEntryDto(
    id = this.id,
    taskId = 1L, //todo: pass in the Object
    username = this.employeeName,
    hoursWorked = this.hoursWorked,
    workDate = this.workDate,
    notes = this.notes,
    submittedAt = this.submittedAt
)

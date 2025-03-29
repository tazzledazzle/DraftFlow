package com.northshore.timesheet.service.impl

import com.northshore.timesheet.dto.TaskHoursDto
import com.northshore.timesheet.dto.TimesheetEntryCreateDto
import com.northshore.timesheet.dto.TimesheetEntryDto
import com.northshore.timesheet.dto.toDto
import com.northshore.timesheet.repository.TaskRepository
import com.northshore.timesheet.repository.TimesheetEntryRepository
import kotlinx.datetime.LocalDate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


interface TimesheetService {
    fun getEntriesByTask(taskId: Long): List<TimesheetEntryDto>
    fun getEntriesByProject(projectId: Long): List<TimesheetEntryDto>
    fun getEntriesByDateRange(startDate: LocalDate, endDate: LocalDate): List<TimesheetEntryDto>
    fun createEntry(entryDto: TimesheetEntryCreateDto): TimesheetEntryDto
    fun submitBatchEntries(entries: List<TimesheetEntryCreateDto>): List<TimesheetEntryDto>
    fun getTaskHoursByProject(projectId: Long): List<TaskHoursDto>
}

@Service
class TimesheetServiceImpl(
    private val timesheetEntryRepository: TimesheetEntryRepository,
    private val taskRepository: TaskRepository
) : TimesheetService {

    override fun getEntriesByTask(taskId: Long): List<TimesheetEntryDto> {
        return timesheetEntryRepository.findByTaskId(taskId)
            .map { it.toDto() }
    }

    override fun getEntriesByProject(projectId: Long): List<TimesheetEntryDto> {
        return timesheetEntryRepository.findByTaskProjectId(projectId)
            .map { it.toDto() }
    }

    override fun getEntriesByDateRange(startDate: LocalDate, endDate: LocalDate): List<TimesheetEntryDto> {
        return timesheetEntryRepository.findByWorkDateBetween(startDate, endDate)
            .map { it.toDto() }
    }

    @Transactional
    override fun createEntry(entryDto: TimesheetEntryCreateDto): TimesheetEntryDto {
        val task = taskRepository.findById(entryDto.taskId)
            .orElseThrow { ResourceNotFoundException("Task not found with id: ${entryDto.taskId}") }

        val entry = entryDto.toEntity(task)
        val savedEntry = timesheetEntryRepository.save(entry)
        return savedEntry.toDto()
    }

    @Transactional
    override fun submitBatchEntries(entries: List<TimesheetEntryCreateDto>): List<TimesheetEntryDto> {
        return entries.map { createEntry(it) }
    }

    override fun getTaskHoursByProject(projectId: Long): List<TaskHoursDto> {
        // Calculate hours per task for a given project
        val timesheetEntries = timesheetEntryRepository.findByTaskProjectId(projectId)

        // Group by task and sum hours
        return timesheetEntries
            .groupBy { it.task!!.id!! }
            .map { (taskId, entries) ->
                val task = entries.first().task!!
                TaskHoursDto(
                    taskId = taskId,
                    taskName = task.name,
                    totalHours = entries.sumOf { it.hoursWorked }
                )
            }
    }
}
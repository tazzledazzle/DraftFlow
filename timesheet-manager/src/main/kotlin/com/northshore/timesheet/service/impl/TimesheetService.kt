package com.northshore.timesheet.service.impl

import com.northshore.timesheet.dto.TaskHoursDto
import com.northshore.timesheet.dto.TimesheetEntryCreateDto
import com.northshore.timesheet.dto.TimesheetEntryDto
import kotlinx.datetime.LocalDate


interface TimesheetService {
    fun getEntriesByTask(taskId: Long): List<TimesheetEntryDto>
    fun getEntriesByProject(projectId: Long): List<TimesheetEntryDto>
    fun getEntriesByDateRange(startDate: LocalDate, endDate: LocalDate): List<TimesheetEntryDto>
    fun createEntry(entryDto: TimesheetEntryCreateDto): TimesheetEntryDto
    fun submitBatchEntries(entries: List<TimesheetEntryCreateDto>): List<TimesheetEntryDto>
    fun getTaskHoursByProject(projectId: Long): List<TaskHoursDto>
}
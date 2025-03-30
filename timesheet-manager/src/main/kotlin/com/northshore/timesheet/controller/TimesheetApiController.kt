package com.northshore.timesheet.controller

import com.northshore.timesheet.dto.TaskHoursDto
import com.northshore.timesheet.dto.TimesheetEntryCreateDto
import com.northshore.timesheet.dto.TimesheetEntryDto
import com.northshore.timesheet.service.impl.TimesheetService
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.time.LocalDate

@RestController
@RequestMapping("/api/timesheets")
class TimesheetApiController(private val timesheetService: TimesheetService) {

    @GetMapping("/task/{taskId}")
    fun getEntriesByTask(@PathVariable taskId: Long): ResponseEntity<List<TimesheetEntryDto>> {
        val entries = timesheetService.getEntriesByTask(taskId)
        return ResponseEntity.ok(entries)
    }

    @GetMapping("/project/{projectId}")
    fun getEntriesByProject(@PathVariable projectId: Long): ResponseEntity<List<TimesheetEntryDto>> {
        val entries = timesheetService.getEntriesByProject(projectId)
        return ResponseEntity.ok(entries)
    }

    @GetMapping("/date-range")
    fun getEntriesByDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<List<TimesheetEntryDto>> {
        val entries = timesheetService.getEntriesByDateRange(startDate, endDate)
        return ResponseEntity.ok(entries)
    }

    @GetMapping("/hours/project/{projectId}")
    fun getTaskHoursByProject(@PathVariable projectId: Long): ResponseEntity<List<TaskHoursDto>> {
        val taskHours = timesheetService.getTaskHoursByProject(projectId)
        return ResponseEntity.ok(taskHours)
    }

    @PostMapping
    fun createEntry(@Valid @RequestBody entryDto: TimesheetEntryCreateDto): ResponseEntity<TimesheetEntryDto> {
        val created = timesheetService.createEntry(entryDto)
        return ResponseEntity
            .created(URI.create("/api/timesheets/${created.id}"))
            .body(created)
    }

    @PostMapping("/batch")
    fun submitBatchEntries(@Valid @RequestBody entries: List<TimesheetEntryCreateDto>): ResponseEntity<List<TimesheetEntryDto>> {
        val created = timesheetService.submitBatchEntries(entries)
        return ResponseEntity.ok(created)
    }
}
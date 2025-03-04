package com.northshore.controllers


import com.northshore.dto.TimesheetEntryDto
import com.northshore.dto.TimesheetSubmitDto
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import services.TimesheetEntryService
import java.net.URI
import java.time.LocalDate

@RestController
@RequestMapping("/api/timesheets")
class TimesheetApiController(private val timesheetService: TimesheetEntryService) {

    @PostMapping
    fun submitTimesheetEntry(@Valid @RequestBody entryDto: TimesheetSubmitDto): ResponseEntity<TimesheetEntryDto> {
        val submitted = timesheetService.submitTimesheetEntry(entryDto)
        return ResponseEntity
            .created(URI.create("/api/timesheets/${submitted.id}"))
            .body(submitted)
    }

    @PostMapping("/batch")
    fun submitBatchEntries(
        @Valid @RequestBody entries: List<TimesheetEntryDto>
    ): ResponseEntity<List<TimesheetEntryDto>> {
        val submitted = timesheetService.submitTimesheetBatch(entries)
        return ResponseEntity.ok(submitted)
    }

    @GetMapping("/project/{projectId}")
    fun getEntriesByProject(
        @PathVariable projectId: Long,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ): ResponseEntity<List<TimesheetEntryDto>> {
        val entries = timesheetService.getEntriesByProject(projectId, startDate, endDate)
        return ResponseEntity.ok(entries)
    }

    @GetMapping("/task/{taskId}")
    fun getEntriesByTask(
        @PathVariable taskId: Long,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ): ResponseEntity<List<TimesheetEntryDto>> {
        val entries = timesheetService.getEntriesByTask(taskId, startDate, endDate)
        return ResponseEntity.ok(entries)
    }

    @GetMapping("/employee/{employeeName}")
    fun getEntriesByEmployee(
        @PathVariable employeeName: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ): ResponseEntity<List<TimesheetEntryDto>> {
        val entries = timesheetService.getEntriesByEmployee(employeeName, startDate, endDate)
        return ResponseEntity.ok(entries)
    }

    @GetMapping("/{id}")
    fun getEntryById(@PathVariable id: Long): ResponseEntity<TimesheetEntryDto> {
        return timesheetService.getEntryById(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}")
    fun deleteEntry(@PathVariable id: Long): ResponseEntity<Void> {
        val deleted = timesheetService.deleteEntry(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/project/{projectId}/summary")
    fun getProjectTimesheetSummary(
        @PathVariable projectId: Long,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ): ResponseEntity<TimesheetSummaryDto> {
        val summary = timesheetService.getProjectTimesheetSummary(projectId, startDate, endDate)
        return ResponseEntity.ok(summary)
    }

    @GetMapping("/project/{projectId}/task-summary")
    fun getTaskHoursByProject(@PathVariable projectId: Long): ResponseEntity<List<TaskHoursDto>> {
        val taskHours = timesheetService.getTaskHoursByProject(projectId)
        return ResponseEntity.ok(taskHours)
    }
}
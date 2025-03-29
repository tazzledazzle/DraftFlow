package com.northshore.timesheet.repository

import com.northshore.timesheet.entity.TimesheetEntry
import kotlinx.datetime.LocalDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param


interface TimesheetEntryRepository : JpaRepository<TimesheetEntry, Long> {
    fun findByTaskId(taskId: Long): List<TimesheetEntry>

    @Query("SELECT te FROM TimesheetEntry te WHERE te.task.project.id = :projectId")
    fun findByTaskProjectId(@Param("projectId") projectId: Long): List<TimesheetEntry>

    fun findByWorkDateBetween(startDate: LocalDate, endDate: LocalDate): List<TimesheetEntry>
}
package com.northshore.repository

import com.northshore.dto.TaskHoursDto
import com.northshore.dto.TimesheetEntryDto
import com.northshore.dto.WeeklyTimesheetDto
import com.northshore.models.TimesheetEntry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface TimesheetEntryRepository : JpaRepository<TimesheetEntry, Long> {
    fun findByTaskId(taskId: Long): List<TimesheetEntryDto>

    fun findByProjectId(projectId: Long): List<TimesheetEntryDto>

    fun findByWorkDateBetween(startDate: LocalDate, endDate: LocalDate): List<TimesheetEntryDto>

//    @Query("SELECT NEW TaskHoursDto(t.id, t.name, SUM(te.hoursWorked)) " +
//            "FROM Task t JOIN t.timesheetEntries te " +
//            "WHERE t.project.id = :projectId " +
//            "GROUP BY t.id, t.name " +
//            "ORDER BY SUM(te.hoursWorked) DESC")
    fun getTaskHoursByProject(@Param("projectId") projectId: Long): List<TaskHoursDto>

    fun findByEmployeeNameAndWorkDateBetween(employeeName: String, weekStartDate: LocalDate, weekEndDate: LocalDate): List<WeeklyTimesheetDto>


}
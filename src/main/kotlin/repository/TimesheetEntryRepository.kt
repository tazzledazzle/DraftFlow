package repository

import dto.TaskHoursDto
import models.TimesheetEntry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface TimesheetEntryRepository : JpaRepository<TimesheetEntry, Long> {
    fun findByTaskId(taskId: Long): List<TimesheetEntry>

    fun findByTaskProjectId(projectId: Long): List<TimesheetEntry>

    fun findByWorkDateBetween(startDate: LocalDate, endDate: LocalDate): List<TimesheetEntry>

    //    @Query("SELECT NEW com.yourcompany.timesheet.dto.TaskHoursDto(t.id, t.name, SUM(te.hoursWorked)) " +
//            "FROM Task t JOIN t.timesheetEntries te " +
//            "WHERE t.projectId = :projectId " +
//            "GROUP BY t.id, t.name")
    fun getTaskHoursByProject(@Param("projectId") projectId: Long): List<TaskHoursDto>
}

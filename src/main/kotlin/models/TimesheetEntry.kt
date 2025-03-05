package com.northshore.models

import dto.ProjectDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "timesheet_entries")
data class TimesheetEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column
    var taskId: Long = 0L, // todo: Task Object
    @Column(name = "employee_name")
    var employeeName: String = "",
    @Column
    var username: String = "",
    @Column(name = "hours_worked")
    var hoursWorked: Double = 0.0,
    @Column
    var weekStartDate: LocalDate,


    @Column(name = "work_date")
    var workDate: LocalDate = LocalDate.now(),
    @Column
    var notes: String? = "",
    @Column(name = "submitted_at")
    var submittedAt: LocalDateTime? = null


)
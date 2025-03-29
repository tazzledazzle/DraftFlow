// src/main/kotlin/com/yourcompany/timesheet/entity/TimesheetEntry.kt
package com.northshore.timesheet.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


@Entity
@Table(name = "timesheet_entries")
@Serializable
class TimesheetEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "employee_name", nullable = false)
    var employeeName: String,

    @Column(name = "hours_worked", nullable = false)
    var hoursWorked: Double,

    @Column(name = "work_date", nullable = false)
    var workDate: String,

    @Column
    var notes: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    var task: Task? = null,

    @Column(name = "submitted_at", nullable = false)
    val submittedAt: String = ""
)
// src/main/kotlin/com/yourcompany/timesheet/entity/Task.kt
package com.northshore.timesheet.entity

import jakarta.persistence.*
import kotlinx.datetime.Clock.System
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import java.util.Date

@Entity
@Serializable
class Task {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Column(nullable = false)
    var name: String? = null

    @Column
    var description: String? = null

    @Column(name = "estimated_hours")
    var estimatedHours: Double? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    var project: Project? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt : String = System.now().toLocalDateTime(TimeZone.UTC).toString()

    @OneToMany(mappedBy = "task", cascade = [CascadeType.ALL], orphanRemoval = true)
    val timesheetEntries: MutableList<TimesheetEntry> = mutableListOf()

    fun addTimesheetEntry(entry: TimesheetEntry) {
        timesheetEntries.add(entry)
        entry.task = this
    }

    fun removeTimesheetEntry(entry: TimesheetEntry) {
        timesheetEntries.remove(entry)
        entry.task = null
    }
}
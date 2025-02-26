package models

import java.time.LocalDate
import java.time.LocalDateTime

data class TimesheetEntry(
    var id: Long? = null,
    var taskId: Long = 0L,
    var employeeName: String = "",
    var hoursWorked: Double = 0.0,
    var workDate: LocalDate = LocalDate.now(),
    var notes: String? = "",
    var submittedAt: LocalDateTime? = null
)
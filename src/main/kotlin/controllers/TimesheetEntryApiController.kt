package controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

/*
        Endpoint	Method	Description	Request	Response
        /api/timesheets	POST	Submit timesheet entry	TimesheetEntryDto	TimesheetEntryDto
        /api/timesheets/project/{projectId}	GET	Get timesheet entries for project	-	Array of TimesheetEntryDto
        /api/timesheets/task/{taskId}	GET	Get timesheet entries for task	-	Array of TimesheetEntryDto

*/
class TimesheetEntryApiController {
    @PostMapping("/api/timesheets")
    fun submitTimesheetEntry() {
        TODO()
    }

    @GetMapping("/api/timesheets/project/{projectId}")
    fun getTimesheetEntriesForProject() {
        TODO()
    }

    @GetMapping("/api/timesheets/task/{taskId}")
    fun getTimesheetEntriesForTask() {
        TODO()
    }
}
package com.northshore.dto

data class EmployeeHoursDto (
    val employeeName: String,
    val hoursWorked: Double? = null,
    val week: Int? = null,
    val year: Int? = null,
    val totalHours: Double? = null

)
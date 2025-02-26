package dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero

data class TaskDto (
    var id: Long? = null,
    var projectId: Long = 0L,
    var name: String = "",
    var description: String = "",
    var estimatedHours: Double = 0.0,
    var createdAt: String = ""
)

data class TaskCreateDto(
    @field:NotBlank(message = "Name is required")
    val name: String,
    @field:NotNull(message = "Project ID is required")
    val projectId: Long,

    val description: String,

    @field:PositiveOrZero(message = "Estimated hours must be 0 or greater")
    val estimatedHours: Double? = null
)

data class TaskUpdateDto(
    @field:NotBlank(message = "Name is required")
    val name: String,
    @field:NotNull(message = "Project ID is required")
    val projectId: Long,

    val description: String,

    @field:PositiveOrZero(message = "Estimated hours must be 0 or greater")
    val estimatedHours: Double? = null
)
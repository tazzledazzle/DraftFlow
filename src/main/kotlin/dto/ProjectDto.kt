package dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

data class ProjectDto(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val projectManagerId: Long? = null,
    val taskCount: Int = 0,
    val createdAt: LocalDateTime? = null
)

data class ProjectCreateDto(
    @field:NotBlank(message = "Name is required")
    val name: String,
    val description: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    @field:NotNull(message = "Project Manager ID is required")
    val projectManagerId: Long? = null
)
data class ProjectUpdateDto(
    @field:NotBlank(message = "Name is required")
    val name: String,
    val description: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    @field:NotNull(message = "Project Manager ID is required")
    val projectManagerId: Long? = null
)
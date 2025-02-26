package dto

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

)
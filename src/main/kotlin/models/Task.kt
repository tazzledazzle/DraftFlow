package models

data class Task (
    var id: String,
    var projectId: String,
    var name: String,
    var description: String,
    var estimatedHours: Int,
    var createdAt: String
)
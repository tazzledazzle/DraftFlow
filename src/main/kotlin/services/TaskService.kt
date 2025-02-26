package services

import models.Task

class TaskService {
    fun getTasksByProject(projectId: Long) : List<Task> {
        TODO()
        return listOf<Task>()
    }

    fun getTaskById(taskId: Long): Task? {
        TODO("Not yet implemented")
        return Task() //not implemented
    }

    fun createTask(value: Any): Task {
            TODO("Not yet implemented")
            return Task() //not implemented
    }

    fun updateTask(lng: Long, value: Any): Task? {
        TODO("Not yet implemented")
    }

    fun deleteTask(lng: Long) : Boolean {
        TODO("Not yet implemented")
        return false
    }


}
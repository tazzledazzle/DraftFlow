package models

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe

class TaskTest : ExpectSpec({
    expect("has a name") {
        val task = Task(1, 1, "Task 1", "Description", 10.0, "2021-01-01")
        task.name shouldBe "Task 1"
    }

    expect("has a description") {
        val task = Task(1, 1, "Task 1", "Description", 10.0, "2021-01-01")
        task.description shouldBe "Description"
    }

    expect("has estimated hours") {
        val task = Task(1, 1, "Task 1", "Description", 10.0, "2021-01-01")
        task.estimatedHours shouldBe 10
    }

    expect("has a project id") {
        val task = Task(1, 1, "Task 1", "Description", 10.0, "2021-01-01")
        task.projectId shouldBe 1
    }

    expect("has a created at date") {
        val task = Task(1, 1, "Task 1", "Description", 10.0, "2021-01-01")
        task.createdAt shouldBe "2021-01-01"
    }

    expect("has an id") {
        val task = Task(1, 1, "Task 1", "Description", 10.0, "2021-01-01")
        task.id shouldBe 1
    }

    expect("can be converted to a string") {
        val task = Task(1, 1, "Task 1", "Description", 10.0, "2021-01-01")
        task.toString() shouldBe "Task(id=1, projectId=1, name=Task 1, description=Description, estimatedHours=10, createdAt=2021-01-01)"
    }

    xexpect("can be converted to a map") {
        val task = Task(1, 1, "Task 1", "Description", 10.0, "2021-01-01")
        val taskMap = mapOf(
            "id" to 1,
            "projectId" to 1,
            "name" to "Task 1",
            "description" to "Description",
            "estimatedHours" to 10.0,
            "createdAt" to "2021-01-01"
        )

        taskMap["id"] shouldBe 1
        taskMap["projectId"] shouldBe 1
        taskMap["name"] shouldBe "Task 1"
        taskMap["description"] shouldBe "Description"
        taskMap["estimatedHours"] shouldBe 10
        taskMap["createdAt"] shouldBe "2021-01-01"
    }


})
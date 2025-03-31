import io.kotest.core.annotation.AutoScan
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import com.northshore.models.Task
import java.time.LocalDateTime

@AutoScan
class TaskTest : ExpectSpec({
    xexpect("has a name") {
        val task = Task(1, 1, "Task 1", "Description", 10.0, LocalDateTime.of(2021,1,1,0,0) as Double?)
        task.name shouldBe "Task 1"
    }

    xexpect("has a description") {
        val task = Task(1, 1, "Task 1", "Description", 10.0, LocalDateTime.of(2021,1,1,0,0) as Double?)
        task.description shouldBe "Description"
    }

    xexpect("has estimated hours") {
        val task = Task(1, 1, "Task 1", "Description", 10.0, LocalDateTime.of(2021,1,1,0,0) as Double?)
        task.estimatedHours shouldBe 10
    }

    xexpect("has a project id") {
        val task = Task(1, 1, "Task 1", "Description", 10.0, LocalDateTime.of(2021,1,1,0,0) as Double?)
        task.projectId shouldBe 1
    }

    xexpect("has a created at date") {
        val task = Task(1, 1, "Task 1", "Description", 10.0, LocalDateTime.of(2021,1,1,0,0) as Double?)
        task.createdAt shouldBe LocalDateTime.of(2021,1,1,0,0)
    }

    xexpect("has an id") {
        val task = Task(1, 1, "Task 1", "Description", 10.0, LocalDateTime.of(2021,1,1,0,0) as Double?)
        task.id shouldBe 1
    }

    xexpect("can be converted to a string") {
        val task = Task(1, 1, "Task 1", "Description", 10.0, LocalDateTime.of(2021,1,1,0,0) as Double?)
        task.toString() shouldBe "Task(id=1, projectId=1, name=Task 1, description=Description, estimatedHours=10.0, createdAt=2021-01-01T00:00)"
    }

    xexpect("can be converted to a map") {
        val task = Task(1, 1, "Task 1", "Description", 10.0, LocalDateTime.of(2021,1,1,0,0) as Double?)
        val taskMap = mapOf(
            "id" to 1,
            "projectId" to 1,
            "name" to "Task 1",
            "description" to "Description",
            "estimatedHours" to 10.0,
            "createdAt" to LocalDateTime.of(2021,1,1,0,0)
        )

        taskMap["id"] shouldBe 1
        taskMap["projectId"] shouldBe 1
        taskMap["name"] shouldBe "Task 1"
        taskMap["description"] shouldBe "Description"
        taskMap["estimatedHours"] shouldBe 10
        taskMap["createdAt"] shouldBe LocalDateTime.of(2021,1,1,0,0)
    }


})
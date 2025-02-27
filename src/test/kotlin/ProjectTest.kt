import io.kotest.core.spec.style.ExpectSpec
import models.Project
import java.time.LocalDateTime

class ProjectTest : ExpectSpec({
    context("Project") {
        val project = createProject()

        expect("id is 1") {
            project.id == 1
        }

        expect("projectManagerId is 1") {
            project.projectManagerId == 1
        }

        expect("name is Project 1") {
            project.name == "Project 1"
        }

        expect("description is Description 1") {
            project.description == "Description 1"
        }

        expect("startDate is 2021-01-01") {
            project.startDate == "2021-01-01"
        }

        expect("endDate is 2021-12-31") {
            project.endDate == "2021-12-31"
        }

        expect("createdAt is 2021-01-01") {
            project.createdAt == "2021-01-01"
        }
    }
})
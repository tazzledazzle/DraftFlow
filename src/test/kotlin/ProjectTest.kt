import io.kotest.core.annotation.AutoScan
import io.kotest.core.spec.style.ExpectSpec
import models.Project
import services.ProjectTestController
import java.time.LocalDateTime

@AutoScan
class ProjectTest : ExpectSpec({
    context("Project") {
        val project = ProjectTestController.createProject()

        expect("id is 1") {
            project.id == 1L
        }

        expect("projectManagerId is 1") {
            project.projectManagerId == 1L
        }

        expect("name is Project 1") {
            project.name == "Project 1"
        }

        expect("description is Description 1") {
            project.description == "Description 1"
        }

        expect("startDate is 2021-01-01") {
            project.startDate == LocalDateTime.of(2021,1,1,0,0)
        }

        expect("endDate is 2021-12-31") {
            project.endDate == LocalDateTime.of(2021,12,31,0,0)
        }

        expect("createdAt is 2025-02-27") {
            project.createdAt == LocalDateTime.of(2025,2,27,0,0)
        }
    }
})
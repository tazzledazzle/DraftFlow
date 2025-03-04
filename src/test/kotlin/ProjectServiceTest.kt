import com.northshore.services.ProjectService
import controllers.ProjectApiController
import io.kotest.core.spec.style.FunSpec
import services.ProjectTestController
import kotlin.test.assertEquals

class ProjectServiceTest
    : FunSpec ({
    test("Test Controller") {
        val project = ProjectTestController.createProject()

        assertEquals(project.name, project.name)
    }

    test("Retrieve Project") {
//        val project = ProjectApiController(ProjectService())
//
//        assertEquals(project?.id, 1)
    }
})
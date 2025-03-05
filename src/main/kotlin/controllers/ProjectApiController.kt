package controllers

/*
* Endpoint	Method	Description	Request	Response
/api/projects	GET	Get all projects	-	Array of ProjectDto
/api/projects	POST	Create new project	ProjectCreateDto	ProjectDto
/api/projects/{id}	GET	Get project by ID	-	ProjectDto
/api/projects/{id}	PUT	Update project	ProjectUpdateDto	ProjectDto
/api/projects/{id}	DELETE	Delete project	-	204 No Content
*
* */

import com.northshore.services.ProjectService
import dto.ProjectCreateDto
import dto.ProjectDto
import dto.ProjectUpdateDto
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import java.net.URI

/**
 * Controller for the web interface (using Thymeleaf)
 */
@Controller
@RequestMapping("/projects")
class ProjectController(private val projectService: ProjectService) {

    @GetMapping
    fun listProjects(model: Model): String {
        // Add projects to the model for rendering
        model.addAttribute("projects", projectService.getAllProjects())
        return "projects/list"
    }

    @GetMapping("/create")
    fun createProjectForm(model: Model): String {
        // Add empty project object to the model
        model.addAttribute("project", ProjectCreateDto("", null))
        return "projects/create"
    }

    @PostMapping("/create")
    fun createProject(
        @Valid @ModelAttribute("project") projectDto: ProjectCreateDto,
        bindingResult: BindingResult
    ): String {
        if (bindingResult.hasErrors()) {
            return "projects/create"
        }

        // Create project and redirect to project list
        projectService.createProject(projectDto)
        return "redirect:/projects"
    }

    @GetMapping("/{id}")
    fun viewProject(@PathVariable id: Long, model: Model): String {
        val project = projectService.getProjectById(id)
            ?: return "redirect:/projects"

        model.addAttribute("project", project)
        return "projects/view"
    }

    @GetMapping("/{id}/edit")
    fun editProjectForm(@PathVariable id: Long, model: Model): String {
        val project = projectService.getProjectById(id)
            ?: return "redirect:/projects"

        // Create update DTO from project DTO
        val updateDto = ProjectUpdateDto(
            name = project.name,
            description = project.description,
            startDate = project.startDate,
            endDate = project.endDate,
            projectManagerId = project.projectManagerId
        )

        model.addAttribute("project", updateDto)
        model.addAttribute("projectId", id)
        return "projects/edit"
    }

    @PostMapping("/{id}/edit")
    fun updateProject(
        @PathVariable id: Long,
        @Valid @ModelAttribute("project") projectDto: ProjectUpdateDto,
        bindingResult: BindingResult
    ): String {
        if (bindingResult.hasErrors()) {
            return "projects/edit"
        }

        projectService.updateProject(id, projectDto)
        return "redirect:/projects"
    }

    @PostMapping("/{id}/delete")
    fun deleteProject(@PathVariable id: Long): String {
        projectService.deleteProject(id)
        return "redirect:/projects"
    }

    @GetMapping("/search")
    fun searchProjects(@RequestParam name: String, model: Model): String {
        model.addAttribute("projects", projectService.searchProjectsByName(name))
        model.addAttribute("searchTerm", name)
        return "projects/list"
    }
}

/**
 * REST API controller for Project resources
 */
@RestController
@RequestMapping("/api/projects")
class ProjectApiController(private val projectService: ProjectService) {

    @GetMapping
    fun getAllProjects(): ResponseEntity<List<ProjectDto>> {
        return ResponseEntity.ok(projectService.getAllProjects())
    }

    @GetMapping("/{id}")
    fun getProject(@PathVariable id: Long): ResponseEntity<ProjectDto> {
        return projectService.getProjectById(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @PostMapping
    fun createProject(@Valid @RequestBody projectDto: ProjectCreateDto): ResponseEntity<ProjectDto> {
        val created = projectService.createProject(projectDto)
        return ResponseEntity
            .created(URI.create("/api/projects/${created.id}"))
            .body(created)
    }

    @PutMapping("/{id}")
    fun updateProject(
        @PathVariable id: Long,
        @Valid @RequestBody projectDto: ProjectUpdateDto
    ): ResponseEntity<ProjectDto> {
        return projectService.updateProject(id, projectDto)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}")
    fun deleteProject(@PathVariable id: Long): ResponseEntity<Void> {
        val deleted = projectService.deleteProject(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/manager/{managerId}")
    fun getProjectsByManager(@PathVariable managerId: Long): ResponseEntity<List<ProjectDto>> {
        return ResponseEntity.ok(projectService.getProjectsByManager(managerId))
    }

    @GetMapping("/search")
    fun searchProjects(@RequestParam name: String): ResponseEntity<List<ProjectDto>> {
        return ResponseEntity.ok(projectService.searchProjectsByName(name))
    }

    @GetMapping("/active")
    fun getActiveProjects(): ResponseEntity<List<ProjectDto>> {
        return ResponseEntity.ok(projectService.getActiveProjects())
    }
}
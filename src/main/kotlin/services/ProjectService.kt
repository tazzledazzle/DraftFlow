package com.northshore.services

import com.northshore.repository.UserRepository
import dto.ProjectCreateDto
import dto.ProjectDto
import dto.ProjectUpdateDto
import com.northshore.models.Project
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.northshore.repository.ProjectRepository
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Service interface for Project management operations
 */
interface ProjectService {
    /**
     * Retrieves all projects in the system
     * @return List of project DTOs
     */
    fun getAllProjects(): List<ProjectDto>

    /**
     * Retrieves projects managed by a specific project manager
     * @param projectManagerId The ID of the project manager
     * @return List of project DTOs
     */
    fun getProjectsByManager(projectManagerId: Long): List<ProjectDto>

    /**
     * Retrieves a project by its ID
     * @param id The project ID
     * @return The project DTO or null if not found
     */
    fun getProjectById(id: Long): ProjectDto?

    /**
     * Creates a new project
     * @param projectDto The project creation data
     * @return The created project DTO
     */
    fun createProject(projectDto: ProjectCreateDto): ProjectDto

    /**
     * Updates an existing project
     * @param id The project ID
     * @param projectDto The project update data
     * @return The updated project DTO or null if not found
     */
    fun updateProject(id: Long, projectDto: ProjectUpdateDto): ProjectDto?

    /**
     * Deletes a project
     * @param id The project ID
     * @return true if deleted, false if not found
     */
    fun deleteProject(id: Long): Boolean

    /**
     * Searches for projects by name
     * @param name The project name to search for
     * @return List of matching project DTOs
     */
    fun searchProjectsByName(name: String): List<ProjectDto>

    /**
     * Retrieves currently active projects
     * @return List of active project DTOs
     */
    fun getActiveProjects(): List<ProjectDto>
}

/**
 * Implementation of the ProjectService interface
 */
@Service
class ProjectServiceImpl(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository
) : ProjectService {

    override fun getAllProjects(): List<ProjectDto> {
        return projectRepository.findAll().map { it.toDto() }
    }

    override fun getProjectsByManager(projectManagerId: Long): List<ProjectDto> {
        return projectRepository.findByProjectManagerId(projectManagerId).map { it.toDto() }
    }

    override fun getProjectById(id: Long): ProjectDto? {
        return projectRepository.findById(id)
            .map { it.toDto() }
            .orElse(null)
    }

    @Transactional
    override fun createProject(projectDto: ProjectCreateDto): ProjectDto {
        // Validate project data
        validateProjectData(
            name = projectDto.name,
            startDate = projectDto.startDate,
            endDate = projectDto.endDate
        )

        // Create project entity
        val project = Project(
            name = projectDto.name,
            description = projectDto.description!!,
            startDate = projectDto.startDate as LocalDate?,
            endDate = projectDto.endDate as LocalDate?
        )

        // Associate project manager if provided
        projectDto.projectManagerId?.let { pmId ->
            val projectManager = userRepository.findById(pmId)
                .orElseThrow { Exception("Project manager not found with id: $pmId") }
            project.projectManager = projectManager
        }

        // Save and return the project
        val savedProject = projectRepository.save(project)
        return savedProject.toDto()
    }

    @Transactional
    override fun updateProject(id: Long, projectDto: ProjectUpdateDto): ProjectDto? {
        // Validate project data
        validateProjectData(
            name = projectDto.name,
            startDate = projectDto.startDate,
            endDate = projectDto.endDate
        )

        return projectRepository.findById(id).map { project ->
            // Update project properties
            project.name = projectDto.name
            project.description = projectDto.description!!
            project.startDate = projectDto.startDate as LocalDate?
            project.endDate = projectDto.endDate as LocalDate?

            // Update project manager if provided
            projectDto.projectManagerId?.let { pmId ->
                val projectManager = userRepository.findById(pmId)
                    .orElseThrow { Exception("Project manager not found with id: $pmId") }
                project.projectManager = projectManager
            }

            // Save and return updated project
            projectRepository.save(project).toDto()
        }.orElse(null)
    }

    @Transactional
    override fun deleteProject(id: Long): Boolean {
        return if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    override fun searchProjectsByName(name: String): List<ProjectDto> {
        return projectRepository.findByNameContainingIgnoreCase(name).map { it.toDto() }
    }

    override fun getActiveProjects(): List<ProjectDto> {
        val today = LocalDate.now()
        return projectRepository.findActiveProjects(today).map { it.toDto() }
    }

    /**
     * Validates project data
     * @throws InvalidDataException if any validation fails
     */
    private fun validateProjectData(name: String, startDate: LocalDateTime?, endDate: LocalDateTime?) {
        if (name.isBlank()) {
            throw Exception("Project name cannot be empty")
        }

        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw Exception("End date cannot be before start date")
        }
    }

    /**
     * Extension function to convert Project entity to ProjectDto
     */
    private fun Project.toDto(): ProjectDto {
        return ProjectDto(
            id = this.id,
            name = this.name,
            description = this.description,
            startDate = this.startDate as LocalDateTime?,
            endDate = this.endDate as LocalDateTime?,
            projectManagerId = this.projectManager?.id,
            taskCount = this.tasks.size,
            createdAt = this.createdAt
        )
    }
}
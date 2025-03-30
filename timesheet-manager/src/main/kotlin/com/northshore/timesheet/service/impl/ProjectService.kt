package com.northshore.timesheet.service.impl

import com.northshore.timesheet.dto.ProjectCreateDto
import com.northshore.timesheet.dto.ProjectDto
import com.northshore.timesheet.dto.ProjectUpdateDto
import com.northshore.timesheet.dto.toDto
import com.northshore.timesheet.dto.toEntity
import com.northshore.timesheet.repository.ProjectRepository
import com.northshore.timesheet.repository.UserRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


interface ProjectService {
    fun getAllProjects(): List<ProjectDto>
    fun getProjectById(id: Long): ProjectDto?
    fun getProjectsByManager(managerId: Long): List<ProjectDto>
    fun createProject(projectDto: ProjectCreateDto): ProjectDto
    fun updateProject(id: Long, projectDto: ProjectUpdateDto): ProjectDto?
    fun deleteProject(id: Long): Boolean
    fun getActiveProjects(date: String? = null): List<ProjectDto>
    fun findProjectsByName(name: String): List<ProjectDto>
}


@Service
class ProjectServiceImpl(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository
) : ProjectService {

    override fun getAllProjects(): List<ProjectDto> {
        return projectRepository.findAll().map { it.toDto() }
    }

    override fun getProjectById(id: Long): ProjectDto? {
        return projectRepository.findById(id)
            .map { it.toDto() }
            .orElse(null)
    }

    override fun getProjectsByManager(managerId: Long): List<ProjectDto> {
        return projectRepository.findByProjectManagerId(managerId)
            .map { it.toDto() }
    }

    @Transactional
    override fun createProject(projectDto: ProjectCreateDto): ProjectDto {
        val projectManager = projectDto.projectManagerId?.let { pmId ->
            userRepository.findById(pmId)
                .orElse(null)
        }

        val project = projectDto.toEntity(projectManager)
        val savedProject = projectRepository.save(project)
        return savedProject.toDto()
    }

    @Transactional
    override fun updateProject(id: Long, projectDto: ProjectUpdateDto): ProjectDto? {
        return projectRepository.findById(id).map { project ->
            // Update project properties
            project.name = projectDto.name
            project.description = projectDto.description
            project.startDate = projectDto.startDate
            project.endDate = projectDto.endDate

            // Update project manager if needed
            if (projectDto.projectManagerId != project.projectManager?.id) {
                project.projectManager = projectDto.projectManagerId?.let { pmId ->
                    userRepository.findById(pmId).orElse(null)
                }
            }

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

    override fun getActiveProjects(date: String?): List<ProjectDto> {
        return projectRepository.findActiveProjects(date)
            .map { it.toDto() }
    }

    override fun findProjectsByName(name: String): List<ProjectDto> {
        return projectRepository.findByNameContainingIgnoreCase(name)
            .map { it.toDto() }
    }
}
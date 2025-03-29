package com.northshore.timesheet.service.impl

import com.northshore.timesheet.dto.ProjectCreateDto
import com.northshore.timesheet.dto.ProjectDto
import com.northshore.timesheet.dto.ProjectUpdateDto
import kotlinx.datetime.LocalDate


interface ProjectService {
    fun getAllProjects(): List<ProjectDto>
    fun getProjectById(id: Long): ProjectDto?
    fun getProjectsByManager(managerId: Long): List<ProjectDto>
    fun createProject(projectDto: ProjectCreateDto): ProjectDto
    fun updateProject(id: Long, projectDto: ProjectUpdateDto): ProjectDto?
    fun deleteProject(id: Long): Boolean
    fun getActiveProjects(date: LocalDate = LocalDate.now()): List<ProjectDto>
    fun findProjectsByName(name: String): List<ProjectDto>
}
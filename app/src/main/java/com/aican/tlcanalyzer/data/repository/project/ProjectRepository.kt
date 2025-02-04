package com.aican.tlcanalyzer.data.repository.project

import com.aican.tlcanalyzer.data.database.project.dao.ProjectDetailsDao
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.data.database.project.entities.ProjectDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.sign

class ProjectRepository @Inject constructor(private val projectDetailsDao: ProjectDetailsDao) {

    // Observe all projects as Flow
    fun observeAllProjects(): Flow<List<ProjectDetails>> = projectDetailsDao.observeAllProjects()

    // Fetch all projects one-time
    suspend fun getAllProjects(): List<ProjectDetails> = projectDetailsDao.getAllProjects()

    suspend fun getProjectType(projectId: String): Boolean =
        projectDetailsDao.getProjectType(projectId)

    // Observe a single project by ID as Flow
    fun observeProjectById(projectId: String): Flow<ProjectDetails?> =
        projectDetailsDao.observeProjectById(projectId)

    // Fetch a single project by ID one-time
    suspend fun getProjectById(projectId: String): ProjectDetails? =
        projectDetailsDao.getProjectById(projectId)

    // Insert or update a project
    suspend fun insertProject(project: ProjectDetails) =
        projectDetailsDao.insertProjectDetails(project)

    // Update an existing project
    suspend fun updateProject(project: ProjectDetails) =
        projectDetailsDao.updateProjectById(project)

    // Delete a project by ID
    suspend fun deleteProjectById(projectId: String) =
        projectDetailsDao.deleteProjectById(projectId)

    // Delete all projects
    suspend fun deleteAllProjects() = projectDetailsDao.deleteAllProjects()

    // Observe project count as Flow
    fun observeProjectCount(): Flow<Int> =
        projectDetailsDao.observeProjectCount()

    // Fetch project count one-time
    suspend fun getProjectCount(): Int = projectDetailsDao.observeProjectCount().first()

    suspend fun getNumberOfRfCountsByProjectId(projectId: String): Int =
        projectDetailsDao.getNumberOfRfCountsByProjectId(projectId)

    fun observeNumberOfRfCountsByProjectId(projectId: String): Flow<Int> =
        projectDetailsDao.observeNumberOfRfCountsByProjectId(projectId)

    suspend fun updateNumberOfRfCountsByProjectId(projectId: String, rfCounts: Int) =
        projectDetailsDao.updateNumberOfRfCountsByProjectId(projectId, rfCounts)

    suspend fun getAllMainImagesByProjectId(projectId: String): List<Image> =
        projectDetailsDao.getAllMainImagesByProjectId(projectId)


    suspend fun getAllSplitImageByProjectId(projectId: String): List<Image> =
        projectDetailsDao.getAllSplitImageByProjectId(projectId)
}

package com.aican.tlcanalyzer.data.database.project.dao

import androidx.room.*
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.data.database.project.entities.ProjectDetails
import com.aican.tlcanalyzer.data.database.project.relation.ProjectWithImages
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDetailsDao {

    // Insert or replace project details
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectDetails(projectDetails: ProjectDetails)

    // Insert a list of projects
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectList(projects: List<ProjectDetails>)

    // Observe a single project by ID
    @Query("SELECT * FROM ProjectDetails WHERE projectId = :projectId")
    fun observeProjectById(projectId: String): Flow<ProjectDetails?>

    // One-time fetch of a single project by ID
    @Query("SELECT * FROM ProjectDetails WHERE projectId = :projectId")
    suspend fun getProjectById(projectId: String): ProjectDetails?

    // Update an existing project
    @Update
    suspend fun updateProjectById(project: ProjectDetails)

    // Delete a single project by ID
    @Query("DELETE FROM ProjectDetails WHERE projectId = :projectId")
    suspend fun deleteProjectById(projectId: String)

    // Observe all projects as Flow
    @Query("SELECT * FROM ProjectDetails")
    fun observeAllProjects(): Flow<List<ProjectDetails>>

    // One-time fetch of all projects
    @Query("SELECT * FROM ProjectDetails")
    suspend fun getAllProjects(): List<ProjectDetails>

    // Delete all projects
    @Query("DELETE FROM ProjectDetails")
    suspend fun deleteAllProjects()

    // Check if a project exists by ID
    @Query("SELECT EXISTS(SELECT 1 FROM ProjectDetails WHERE projectId = :projectId)")
    suspend fun doesProjectExist(projectId: String): Boolean

    // Observe count of all projects
    @Query("SELECT COUNT(*) FROM ProjectDetails")
    fun observeProjectCount(): Flow<Int>

    // One-time fetch for count of all projects
    @Query("SELECT COUNT(*) FROM ProjectDetails")
    suspend fun getProjectCount(): Int

    // Get number of RF counts for a specific project
    @Query("SELECT noOfRfCounts FROM ProjectDetails WHERE projectId = :projectId")
    suspend fun getNumberOfRfCountsByProjectId(projectId: String): Int

    @Query("SELECT noOfRfCounts FROM ProjectDetails WHERE projectId = :projectId")
     fun observeNumberOfRfCountsByProjectId(projectId: String): Flow<Int>


    // Update the number of RF counts for a specific project
    @Query("UPDATE ProjectDetails SET noOfRfCounts = :rfCounts WHERE projectId = :projectId")
    suspend fun updateNumberOfRfCountsByProjectId(projectId: String, rfCounts: Int)

    @Query("SELECT imageSplitAvailable FROM ProjectDetails WHERE projectId = :projectId")
    suspend fun getProjectType(projectId: String): Boolean

    @Query("SELECT * FROM Image WHERE projectId = :projectId AND imageType = 'MAIN'")
    suspend fun getAllMainImagesByProjectId(projectId: String): List<Image>

    @Query("SELECT * FROM Image WHERE projectId = :projectId AND imageType = 'SPLIT'")
    suspend fun getAllSplitImageByProjectId(projectId: String): List<Image>

}

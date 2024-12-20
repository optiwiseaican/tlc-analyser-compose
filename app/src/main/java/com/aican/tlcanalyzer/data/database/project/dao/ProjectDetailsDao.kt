package com.aican.tlcanalyzer.data.database.project.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.aican.tlcanalyzer.data.database.project.entities.ProjectDetails
import com.aican.tlcanalyzer.data.database.project.relation.ProjectWithImages

@Dao
interface ProjectDetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectDetails(projectDetails: ProjectDetails)

    @Transaction
    @Query("SELECT * FROM ProjectDetails WHERE id = :projectId")
    suspend fun getProjectWithImages(projectId: String): ProjectWithImages  // Fetch project with its images

    @Update
    suspend fun updateProjectDetails(projectDetails: ProjectDetails)

    @Delete
    suspend fun deleteProjectDetails(projectDetails: ProjectDetails)
}

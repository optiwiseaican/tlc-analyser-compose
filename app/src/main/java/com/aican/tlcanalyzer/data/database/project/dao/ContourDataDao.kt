package com.aican.tlcanalyzer.data.database.project.dao

import androidx.room.*
import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.data.database.project.entities.ContourType
import kotlinx.coroutines.flow.Flow

@Dao
interface ContourDataDao {

    // Observe contours as Flow
    @Query("SELECT * FROM ContourData WHERE imageId = :imageId")
    fun observeAllContoursByImageId(imageId: String): Flow<List<ContourData>>

    // One-time fetch
    @Query("SELECT * FROM ContourData WHERE imageId = :imageId")
    fun getAllContoursByImageId(imageId: String): List<ContourData>

    // Observe a single contour as Flow
    @Query("SELECT * FROM ContourData WHERE contourId = :contourId")
    fun observeContourById(contourId: String): Flow<ContourData?>

    // One-time fetch
    @Query("SELECT * FROM ContourData WHERE contourId = :contourId")
    fun getContourById(contourId: String): ContourData?

    // Count contours (one-time)
    @Query("SELECT COUNT(*) FROM ContourData WHERE imageId = :imageId")
    fun getContoursCountByImageId(imageId: String): Int

    // Observe contours by type as Flow
    @Query("SELECT * FROM ContourData WHERE imageId = :imageId AND type = :type")
    fun observeContoursByType(imageId: String, type: ContourType): Flow<List<ContourData>>

    // One-time fetch by type
    @Query("SELECT * FROM ContourData WHERE imageId = :imageId AND type = :type")
    fun getContoursByType(imageId: String, type: ContourType): List<ContourData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOneContour(contour: ContourData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContours(contours: List<ContourData>)

    @Query("DELETE FROM ContourData WHERE contourId = :contourId")
    suspend fun deleteContourById(contourId: String)

    @Update
    suspend fun updateContourById(contour: ContourData)
}

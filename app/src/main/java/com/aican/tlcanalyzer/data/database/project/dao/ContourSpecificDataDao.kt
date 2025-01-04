package com.aican.tlcanalyzer.data.database.project.dao

import androidx.room.*
import com.aican.tlcanalyzer.data.database.project.entities.ContourSpecificData
import kotlinx.coroutines.flow.Flow

@Dao
interface ContourSpecificDataDao {

    // Observe specific data as Flow
    @Query("SELECT * FROM ContourSpecificData WHERE contourId = :contourId")
    fun observeContourSpecificDataByContourId(contourId: String): Flow<List<ContourSpecificData>>

    // One-time fetch
    @Query("SELECT * FROM ContourSpecificData WHERE contourId = :contourId")
    fun getContourSpecificDataByContourId(contourId: String): List<ContourSpecificData>

    // Observe specific data by Image ID as Flow
    @Query("SELECT * FROM ContourSpecificData WHERE imageId = :imageId")
    fun observeContourSpecificDataByImageId(imageId: String): Flow<List<ContourSpecificData>>

    // One-time fetch
    @Query("SELECT * FROM ContourSpecificData WHERE imageId = :imageId")
    fun getContourSpecificDataByImageId(imageId: String): List<ContourSpecificData>

    // Observe a single result by Image ID and Contour ID as Flow
    @Query("SELECT * FROM ContourSpecificData WHERE imageId = :imageId AND contourId = :contourId LIMIT 1")
    fun observeContourSpecificDataByImageIdAndContourId(
        imageId: String,
        contourId: String
    ): Flow<ContourSpecificData?>

    // One-time fetch
    @Query("SELECT * FROM ContourSpecificData WHERE imageId = :imageId AND contourId = :contourId LIMIT 1")
    fun getContourSpecificDataByImageIdAndContourId(
        imageId: String,
        contourId: String
    ): ContourSpecificData?

    // Observe paginated data as Flow
    @Query("SELECT * FROM ContourSpecificData WHERE imageId = :imageId LIMIT :limit OFFSET :offset")
    fun observePagedContourSpecificData(
        imageId: String,
        limit: Int,
        offset: Int
    ): Flow<List<ContourSpecificData>>

    // One-time paginated fetch
    @Query("SELECT * FROM ContourSpecificData WHERE imageId = :imageId LIMIT :limit OFFSET :offset")
    fun getPagedContourSpecificData(
        imageId: String,
        limit: Int,
        offset: Int
    ): List<ContourSpecificData>

    // Count the number of specific data entries by Image ID as Flow
    @Query("SELECT COUNT(*) FROM ContourSpecificData WHERE imageId = :imageId")
    fun observeSpecificDataCountByImageId(imageId: String): Flow<Int>

    // Count the number of specific data entries by Image ID (one-time fetch)
    @Query("SELECT COUNT(*) FROM ContourSpecificData WHERE imageId = :imageId")
    fun getSpecificDataCountByImageId(imageId: String): Int

    // Check if specific data exists for an Image ID as Flow
    @Query("SELECT EXISTS(SELECT 1 FROM ContourSpecificData WHERE imageId = :imageId)")
    fun observeSpecificDataExists(imageId: String): Flow<Boolean>

    // Check if specific data exists for an Image ID (one-time fetch)
    @Query("SELECT EXISTS(SELECT 1 FROM ContourSpecificData WHERE imageId = :imageId)")
    fun doesSpecificDataExist(imageId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContourSpecificData(data: ContourSpecificData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContourSpecificDataList(data: List<ContourSpecificData>)

    // Update a specific data entry
    @Update
    suspend fun updateContourSpecificData(data: ContourSpecificData)

    // Delete specific data by Contour ID
    @Query("DELETE FROM ContourSpecificData WHERE contourId = :contourId")
    suspend fun deleteContourSpecificDataByContourId(contourId: String)

    // Delete all specific data by Image ID
    @Query("DELETE FROM ContourSpecificData WHERE imageId = :imageId")
    suspend fun deleteAllContourSpecificDataByImageId(imageId: String)

    // Replace all specific data for an Image ID
    @Transaction
    suspend fun replaceSpecificDataByImageId(imageId: String, data: List<ContourSpecificData>) {
        deleteAllContourSpecificDataByImageId(imageId)
        insertContourSpecificDataList(data)
    }
}

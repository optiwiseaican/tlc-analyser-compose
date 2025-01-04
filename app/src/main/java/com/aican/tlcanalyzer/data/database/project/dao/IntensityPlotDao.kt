package com.aican.tlcanalyzer.data.database.project.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.aican.tlcanalyzer.data.database.project.entities.IntensityPlotData
import kotlinx.coroutines.flow.Flow

@Dao
interface IntensityPlotDao {

    // Observe all intensity plot data for an image
    @Query("SELECT * FROM IntensityPlotData WHERE imageId = :imageId")
    fun observeAllIntensityPlots(imageId: String): Flow<List<IntensityPlotData>>

    // One-time fetch
    @Query("SELECT * FROM IntensityPlotData WHERE imageId = :imageId")
    suspend fun getAllIntensityPlots(imageId: String): List<IntensityPlotData>

    // Observe all intensity plot data sorted by RF (ascending)
    @Query("SELECT * FROM IntensityPlotData WHERE imageId = :imageId ORDER BY rf ASC")
    fun observeIntensityPlotsSortedAsc(imageId: String): Flow<List<IntensityPlotData>>

    // Observe all intensity plot data sorted by RF (descending)
    @Query("SELECT * FROM IntensityPlotData WHERE imageId = :imageId ORDER BY rf DESC")
    fun observeIntensityPlotsSortedDesc(imageId: String): Flow<List<IntensityPlotData>>

    // Fetch intensity plot data for a specific RF range (observe)
    @Query("SELECT * FROM IntensityPlotData WHERE imageId = :imageId AND rf BETWEEN :rfStart AND :rfEnd ORDER BY rf ASC")
    fun observeIntensityPlotsByRfRange(imageId: String, rfStart: Double, rfEnd: Double): Flow<List<IntensityPlotData>>

    // Fetch intensity plot data for a specific RF range (one-time)
    @Query("SELECT * FROM IntensityPlotData WHERE imageId = :imageId AND rf BETWEEN :rfStart AND :rfEnd ORDER BY rf ASC")
    suspend fun getIntensityPlotsByRfRange(imageId: String, rfStart: Double, rfEnd: Double): List<IntensityPlotData>

    // Paginated observation
    @Query("SELECT * FROM IntensityPlotData WHERE imageId = :imageId LIMIT :limit OFFSET :offset")
    fun observePagedIntensityPlots(imageId: String, limit: Int, offset: Int): Flow<List<IntensityPlotData>>

    // Paginated one-time fetch
    @Query("SELECT * FROM IntensityPlotData WHERE imageId = :imageId LIMIT :limit OFFSET :offset")
    suspend fun getPagedIntensityPlots(imageId: String, limit: Int, offset: Int): List<IntensityPlotData>

    // Count intensity plot entries (observe)
    @Query("SELECT COUNT(*) FROM IntensityPlotData WHERE imageId = :imageId")
    fun observeIntensityPlotCountByImageId(imageId: String): Flow<Int>

    // Count intensity plot entries (one-time)
    @Query("SELECT COUNT(*) FROM IntensityPlotData WHERE imageId = :imageId")
    suspend fun getIntensityPlotCountByImageId(imageId: String): Int



    // Check if intensity plot data exists for an Image ID (observe)
    @Query("SELECT EXISTS(SELECT 1 FROM IntensityPlotData WHERE imageId = :imageId)")
    fun observeIntensityPlotExists(imageId: String): Flow<Boolean>

    // Check if intensity plot data exists for an Image ID (one-time)
    @Query("SELECT EXISTS(SELECT 1 FROM IntensityPlotData WHERE imageId = :imageId)")
    suspend fun doesIntensityPlotExist(imageId: String): Boolean

    // Insert a single intensity plot data entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleIntensityPlotData(data: IntensityPlotData)

    // Insert multiple intensity plot data entries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntensityPlotData(data: List<IntensityPlotData>)

    // Update a specific intensity plot data entry
    @Query("UPDATE IntensityPlotData SET intensity = :intensity WHERE imageId = :imageId AND rf = :rf")
    suspend fun updateIntensityPlotData(imageId: String, rf: Double, intensity: Double)

    // Replace all intensity plot data for an Image ID
    @Transaction
    suspend fun replaceIntensityPlotData(imageId: String, data: List<IntensityPlotData>) {
        deleteAllIntensityPlotData(imageId)
        insertIntensityPlotData(data)
    }

    // Delete all intensity plot data for a specific Image ID
    @Query("DELETE FROM IntensityPlotData WHERE imageId = :imageId")
    suspend fun deleteAllIntensityPlotData(imageId: String)

    // Delete a specific intensity plot entry by Image ID and RF
    @Query("DELETE FROM IntensityPlotData WHERE imageId = :imageId AND rf = :rf")
    suspend fun deleteIntensityPlotByRf(imageId: String, rf: Double)

    // Delete multiple intensity plot entries for an Image ID within an RF range
    @Query("DELETE FROM IntensityPlotData WHERE imageId = :imageId AND rf BETWEEN :rfStart AND :rfEnd")
    suspend fun deleteIntensityPlotsByRfRange(imageId: String, rfStart: Double, rfEnd: Double)
}

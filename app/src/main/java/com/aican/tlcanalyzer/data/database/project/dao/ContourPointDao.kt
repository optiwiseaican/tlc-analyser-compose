package com.aican.tlcanalyzer.data.database.project.dao

import androidx.room.*
import com.aican.tlcanalyzer.data.database.project.entities.ContourPoint
import kotlinx.coroutines.flow.Flow

@Dao
interface ContourPointDao {

    // Observe points as Flow
    @Query("SELECT * FROM ContourPoints WHERE contourId = :contourId")
    fun observeAllContourPointsByContourId(contourId: String): Flow<List<ContourPoint>>

    // One-time fetch
    @Query("SELECT * FROM ContourPoints WHERE contourId = :contourId")
    fun getAllContourPointsByContourId(contourId: String): List<ContourPoint>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContourPoints(points: List<ContourPoint>)

    @Query("DELETE FROM ContourPoints WHERE contourId = :contourId")
    suspend fun deleteAllPointsByContourId(contourId: String)

    // Count points (observe as Flow)
    @Query("SELECT COUNT(*) FROM ContourPoints WHERE contourId = :contourId")
    fun observePointsCountByContourId(contourId: String): Flow<Int>

    // Count points (one-time fetch)
    @Query("SELECT COUNT(*) FROM ContourPoints WHERE contourId = :contourId")
    fun getPointsCountByContourId(contourId: String): Int
}

package com.aican.tlcanalyzer.data.repository.project

import com.aican.tlcanalyzer.data.database.project.dao.IntensityPlotDao
import com.aican.tlcanalyzer.data.database.project.entities.IntensityPlotData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class IntensityPlotRepository @Inject constructor(
    private val intensityPlotDao: IntensityPlotDao
) {

    // Observe all intensity plots by Image ID as Flow
    fun observeAllIntensityPlots(imageId: String): Flow<List<IntensityPlotData>> =
        intensityPlotDao.observeAllIntensityPlots(imageId)

    // One-time fetch for all intensity plots by Image ID
    suspend fun getAllIntensityPlots(imageId: String): List<IntensityPlotData> =
        intensityPlotDao.getAllIntensityPlots(imageId)

    // Observe paginated intensity plots as Flow
    fun observePagedIntensityPlots(
        imageId: String,
        limit: Int,
        offset: Int
    ): Flow<List<IntensityPlotData>> =
        intensityPlotDao.observePagedIntensityPlots(imageId, limit, offset)

    // One-time fetch for paginated intensity plots
    suspend fun getPagedIntensityPlots(
        imageId: String,
        limit: Int,
        offset: Int
    ): List<IntensityPlotData> =
        intensityPlotDao.getPagedIntensityPlots(imageId, limit, offset)

    // Observe intensity plot count by Image ID as Flow
    fun observeIntensityPlotCount(imageId: String): Flow<Int> =
        intensityPlotDao.observeIntensityPlotCountByImageId(imageId)

    // One-time fetch for intensity plot count by Image ID
    suspend fun getIntensityPlotCount(imageId: String): Int =
        intensityPlotDao.getIntensityPlotCountByImageId(imageId)

    // Insert a list of intensity plot data
    suspend fun insertIntensityPlotData(data: List<IntensityPlotData>) =
        intensityPlotDao.insertIntensityPlotData(data)

    // Update an existing intensity plot data
    suspend fun updateIntensityPlotData(imageId: String, rf: Double, intensity: Double) =
        intensityPlotDao.updateIntensityPlotData(imageId, rf, intensity)

    // Delete all intensity plot data for a given Image ID
    suspend fun deleteAllIntensityPlotData(imageId: String) =
        intensityPlotDao.deleteAllIntensityPlotData(imageId)

    // Replace all intensity plot data for an Image ID
    suspend fun replaceIntensityPlotData(imageId: String, data: List<IntensityPlotData>) {
        intensityPlotDao.deleteAllIntensityPlotData(imageId)
        intensityPlotDao.insertIntensityPlotData(data)
    }

    // Check if intensity plot data exists for a given Image ID as Flow
    fun observeIntensityPlotExists(imageId: String): Flow<Boolean> =
        intensityPlotDao.observeIntensityPlotExists(imageId)

    // One-time check if intensity plot data exists for a given Image ID
    suspend fun doesIntensityPlotExist(imageId: String): Boolean =
        intensityPlotDao.doesIntensityPlotExist(imageId)


}

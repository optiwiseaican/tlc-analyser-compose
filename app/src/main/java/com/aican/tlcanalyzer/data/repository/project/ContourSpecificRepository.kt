package com.aican.tlcanalyzer.data.repository.project

import com.aican.tlcanalyzer.data.database.project.dao.ContourSpecificDataDao
import com.aican.tlcanalyzer.data.database.project.entities.ContourSpecificData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ContourSpecificRepository @Inject constructor(
    private val contourSpecificDataDao: ContourSpecificDataDao
) {

    // Observe specific data by contour ID as Flow
    fun observeSpecificDataByContourId(contourId: String): Flow<List<ContourSpecificData>> =
        contourSpecificDataDao.observeContourSpecificDataByContourId(contourId)

    // One-time fetch specific data by contour ID
    suspend fun getSpecificDataByContourId(contourId: String): List<ContourSpecificData> =
        contourSpecificDataDao.getContourSpecificDataByContourId(contourId)

    // Observe specific data by image ID as Flow
    fun observeSpecificDataByImageId(imageId: String): Flow<List<ContourSpecificData>> =
        contourSpecificDataDao.observeContourSpecificDataByImageId(imageId)

    // One-time fetch specific data by image ID
    suspend fun getSpecificDataByImageId(imageId: String): List<ContourSpecificData> =
        contourSpecificDataDao.getContourSpecificDataByImageId(imageId)

    // Observe specific data by image ID and contour ID as Flow
    fun observeSpecificDataByImageIdAndContourId(
        imageId: String,
        contourId: String
    ): Flow<ContourSpecificData?> =
        contourSpecificDataDao.observeContourSpecificDataByImageIdAndContourId(imageId, contourId)

    // One-time fetch specific data by image ID and contour ID
    suspend fun getSpecificDataByImageIdAndContourId(
        imageId: String,
        contourId: String
    ): ContourSpecificData? =
        contourSpecificDataDao.getContourSpecificDataByImageIdAndContourId(imageId, contourId)

    // Insert a single specific data entry
    suspend fun insertSpecificData(data: ContourSpecificData) =
        contourSpecificDataDao.insertContourSpecificData(data)

    // Insert a list of specific data entries
    suspend fun insertSpecificDataList(data: List<ContourSpecificData>) =
        contourSpecificDataDao.insertContourSpecificDataList(data)

    // Delete specific data by contour ID
    suspend fun deleteSpecificDataByContourId(contourId: String) =
        contourSpecificDataDao.deleteContourSpecificDataByContourId(contourId)

    // Delete all specific data by image ID
    suspend fun deleteAllSpecificDataByImageId(imageId: String) =
        contourSpecificDataDao.deleteAllContourSpecificDataByImageId(imageId)

    // Replace specific data for an image
    suspend fun replaceSpecificDataByImageId(imageId: String, data: List<ContourSpecificData>) {
        contourSpecificDataDao.deleteAllContourSpecificDataByImageId(imageId)
        contourSpecificDataDao.insertContourSpecificDataList(data)
    }
}

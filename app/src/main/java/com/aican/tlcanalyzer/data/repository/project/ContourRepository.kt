package com.aican.tlcanalyzer.data.repository.project

import androidx.room.Transaction
import com.aican.tlcanalyzer.data.database.project.dao.ContourDataDao
import com.aican.tlcanalyzer.data.database.project.dao.ContourPointDao
import com.aican.tlcanalyzer.data.database.project.dao.ManualContourDetailsDao
import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.data.database.project.entities.ContourPoint
import com.aican.tlcanalyzer.data.database.project.entities.ManualContourDetails
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ContourRepository @Inject constructor(
    private val contourDataDao: ContourDataDao,
    private val contourPointDao: ContourPointDao,
    private val manualContourDetailsDao: ManualContourDetailsDao
) {

    suspend fun nukeContourDataTable() = contourDataDao.nukeTable()
    suspend fun nukeContourPointsTable() = contourPointDao.nukeTable()

    suspend fun getManualContourDetailByContourId(contourId: String) =
        manualContourDetailsDao.getManualContourDetailByContourId(contourId)

    @Transaction
    suspend fun clearAllContours(imageId: String) {
        // get all contours by imageId
        val allContours = getAllContoursByImageId(imageId)

        allContours.forEach { contour ->
            deleteAllContourPointsByContourId(contour.contourId)
        }


        // delete all details
        contourDataDao.deleteContoursByImageId(imageId)

//        deleteManualDetailsByContourId(contourId)

    }

    // CRUD Operations for ContourData

    // Observe all contours by image ID as Flow
    fun observeAllContoursByImageId(imageId: String): Flow<List<ContourData>> =
        contourDataDao.observeAllContoursByImageId(imageId)

    // One-time fetch for all contours by image ID
    suspend fun getAllContoursByImageId(imageId: String): List<ContourData> =
        contourDataDao.getAllContoursByImageId(imageId)

    // Delete a contour by its ID
    suspend fun deleteContourById(contourId: String) =
        contourDataDao.deleteContourById(contourId)

    // Update an existing contour
    suspend fun updateContour(contour: ContourData) =
        contourDataDao.updateContourById(contour)

    suspend fun insertContours(contours: List<ContourData>) =
        contourDataDao.insertContours(contours)


    // Insert a new contour
    suspend fun insertContour(contour: ContourData) =
        contourDataDao.insertOneContour(contour)

    // Composite Deletion (Contour + Related Data)
    suspend fun deleteContourWithRelatedData(contourId: String) {
        // Delete related points
        contourPointDao.deleteAllPointsByContourId(contourId)
        // Delete related manual details
        manualContourDetailsDao.deleteManualDetailsByContourId(contourId)
        // Delete the contour itself
        contourDataDao.deleteContourById(contourId)
    }

    // CRUD Operations for ContourPoint

    // Observe all points by contour ID as Flow
    fun observeAllContourPointsByContourId(contourId: String): Flow<List<ContourPoint>> =
        contourPointDao.observeAllContourPointsByContourId(contourId)

    // One-time fetch for all points by contour ID
    suspend fun getAllContourPointsByContourId(contourId: String): List<ContourPoint> =
        contourPointDao.getAllContourPointsByContourId(contourId)

    // Insert a list of contour points
    suspend fun insertContourPoints(points: List<ContourPoint>) =
        contourPointDao.insertContourPoints(points)

    // Delete all points for a given contour ID
    suspend fun deleteAllContourPointsByContourId(contourId: String) =
        contourPointDao.deleteAllPointsByContourId(contourId)


    // CRUD Operations for ManualContourDetails

    // Observe manual details by contour ID as Flow
    fun observeManualDetailsByContourId(contourId: String): Flow<ManualContourDetails?> =
        manualContourDetailsDao.observeManualDetailsByContourId(contourId)

    // One-time fetch for manual details by contour ID
    suspend fun getManualDetailsByContourId(contourId: String): ManualContourDetails? =
        manualContourDetailsDao.getManualDetailsByContourId(contourId)

    // Insert manual contour details
    suspend fun insertManualContourDetails(details: ManualContourDetails) =
        manualContourDetailsDao.insertManualContourDetails(details)

    // Update existing manual contour details
    suspend fun updateManualContourDetails(details: ManualContourDetails) =
        manualContourDetailsDao.updateManualContourDetails(details)


    @Transaction
    suspend fun insertContoursAndPoints(
        contourDataList: List<ContourData>,
        contourPointList: List<ContourPoint>
    ) {
        contourDataDao.deleteContoursByImageId(contourDataList.first().imageId)
        contourDataDao.insertContours(contourDataList)
        contourPointDao.insertContourPoints(contourPointList)
    }

    suspend fun countContoursByImageId(imageId: String): Int =
        contourDataDao.getContoursCountByImageId(imageId)
}

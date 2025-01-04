package com.aican.tlcanalyzer.data.database.project.dao
import androidx.room.*
import com.aican.tlcanalyzer.data.database.project.entities.ManualContourDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ManualContourDetailsDao {

    // Observe manual contour details by Contour ID
    @Query("SELECT * FROM ManualContourDetails WHERE contourId = :contourId")
    fun observeManualDetailsByContourId(contourId: String): Flow<ManualContourDetails?>

    // One-time fetch
    @Query("SELECT * FROM ManualContourDetails WHERE contourId = :contourId")
    suspend fun getManualDetailsByContourId(contourId: String): ManualContourDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManualContourDetails(details: ManualContourDetails)

    @Update
    suspend fun updateManualContourDetails(details: ManualContourDetails)

    @Query("DELETE FROM ManualContourDetails WHERE contourId = :contourId")
    suspend fun deleteManualDetailsByContourId(contourId: String)

    // Observe manual details by Image ID
    @Query(
        """
        SELECT ManualContourDetails.* 
        FROM ManualContourDetails
        INNER JOIN ContourData ON ContourData.contourId = ManualContourDetails.contourId
        WHERE ContourData.imageId = :imageId
    """
    )
    fun observeManualDetailsByImageId(imageId: String): Flow<List<ManualContourDetails>>

    // One-time fetch
    @Query(
        """
        SELECT ManualContourDetails.* 
        FROM ManualContourDetails
        INNER JOIN ContourData ON ContourData.contourId = ManualContourDetails.contourId
        WHERE ContourData.imageId = :imageId
    """
    )
    suspend fun getManualDetailsByImageId(imageId: String): List<ManualContourDetails>
}

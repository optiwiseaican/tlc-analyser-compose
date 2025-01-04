package com.aican.tlcanalyzer.data.database.project.dao

import androidx.room.*
import com.aican.tlcanalyzer.data.database.project.entities.Image
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {

    // Observe all images by Project ID
    @Query("SELECT * FROM Image WHERE projectId = :projectId")
    fun observeAllImagesByProjectId(projectId: String): Flow<List<Image>>

    // One-time fetch
    @Query("SELECT * FROM Image WHERE projectId = :projectId")
    suspend fun getAllImagesByProjectId(projectId: String): List<Image>

    // Observe single image by Image ID
    @Query("SELECT * FROM Image WHERE imageId = :imageId")
    fun observeImageById(imageId: String): Flow<Image?>

    // One-time fetch
    @Query("SELECT * FROM Image WHERE imageId = :imageId")
    suspend fun getImageById(imageId: String): Image?

    @Update
    suspend fun updateImageById(image: Image)

    @Query("DELETE FROM Image WHERE imageId = :imageId")
    suspend fun deleteImageById(imageId: String)

    @Query("DELETE FROM Image WHERE projectId = :projectId")
    suspend fun deleteAllImagesByProjectId(projectId: String)

    // Count images (observe)
    @Query("SELECT COUNT(*) FROM Image WHERE projectId = :projectId")
    fun observeImageCountByProjectId(projectId: String): Flow<Int>

    // Count images (one-time)
    @Query("SELECT COUNT(*) FROM Image WHERE projectId = :projectId")
    suspend fun getImageCountByProjectId(projectId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<Image>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOneImage(image: Image) {
        insertImages(listOf(image))
    }
}

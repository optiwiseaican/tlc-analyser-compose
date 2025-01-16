package com.aican.tlcanalyzer.data.repository.project

import com.aican.tlcanalyzer.data.database.project.dao.ImageDao
import com.aican.tlcanalyzer.data.database.project.entities.Image
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class ImageRepository @Inject constructor(
    private val imageDao: ImageDao
) {

    // Observe all images by Project ID as Flow
    fun observeAllImagesByProjectId(projectId: String): Flow<List<Image>> =
        imageDao.observeAllImagesByProjectId(projectId)

    // One-time fetch for all images by Project ID
    suspend fun getAllImagesByProjectId(projectId: String): List<Image> =
        imageDao.getAllImagesByProjectId(projectId)

    // Observe image count by Project ID as Flow
    fun observeImageCountByProjectId(projectId: String): Flow<Int> =
        imageDao.observeImageCountByProjectId(projectId)

    fun observeImageById(imageId: String): Flow<Image?> =
        imageDao.observeImageById(imageId)

    // One-time fetch for image count by Project ID
    suspend fun getImageCountByProjectId(projectId: String): Int =
        imageDao.getImageCountByProjectId(projectId)

    // Insert a single image
    suspend fun insertImage(image: Image) =
        imageDao.insertOneImage(image)

    // Insert multiple images
    suspend fun insertImages(images: List<Image>) =
        imageDao.insertImages(images)

    // Update an existing image
    suspend fun updateImage(image: Image) =
        imageDao.updateImageById(image)

    // Delete a single image by ID
    suspend fun deleteImageById(imageId: String) =
        imageDao.deleteImageById(imageId)

    // Delete all images by Project ID
    suspend fun deleteAllImagesByProjectId(projectId: String) =
        imageDao.deleteAllImagesByProjectId(projectId)

    // Composite Deletion (Image + Related Data)
    suspend fun deleteImageWithRelatedData(
        imageId: String,
        contourRepository: ContourRepository
    ) {
        // Get contours related to the image
        val contours = contourRepository.observeAllContoursByImageId(imageId)
            .firstOrNull() ?: emptyList()

        // Delete all related contours
        for (contour in contours) {
            contourRepository.deleteContourWithRelatedData(contour.contourId)
        }

        // Delete the image itself
        imageDao.deleteImageById(imageId)
    }


}

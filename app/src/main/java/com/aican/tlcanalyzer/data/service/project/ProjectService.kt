package com.aican.tlcanalyzer.data.service.project

import com.aican.tlcanalyzer.data.repository.project.ContourRepository
import com.aican.tlcanalyzer.data.repository.project.ContourSpecificRepository
import com.aican.tlcanalyzer.data.repository.project.ImageRepository
import com.aican.tlcanalyzer.data.repository.project.IntensityPlotRepository
import com.aican.tlcanalyzer.data.repository.project.ProjectRepository
import javax.inject.Inject

class ProjectService @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val imageRepository: ImageRepository,
    private val contourRepository: ContourRepository,
    private val intensityPlotRepository: IntensityPlotRepository,
    private val contourSpecificDataRepository: ContourSpecificRepository,
) {

    suspend fun deleteProjectWithAllData(projectId: String) {
        val images = imageRepository.getAllImagesByProjectId(projectId)
        for (image in images) {
            imageRepository.deleteImageWithRelatedData(image.imageId, contourRepository)
        }
        projectRepository.deleteProjectById(projectId)
    }
}

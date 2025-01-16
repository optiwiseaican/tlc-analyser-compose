package com.aican.tlcanalyzer.viewmodel.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.data.database.project.entities.ProjectDetails
import com.aican.tlcanalyzer.data.repository.project.ImageRepository
import com.aican.tlcanalyzer.data.repository.project.ProjectRepository
import com.aican.tlcanalyzer.data.service.project.ProjectService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectService: ProjectService,
    private val projectRepository: ProjectRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    val allProjects: Flow<List<ProjectDetails>> = projectRepository.observeAllProjects()

    private val _projects: MutableStateFlow<List<ProjectDetails>> = MutableStateFlow(emptyList())
    val projects: StateFlow<List<ProjectDetails>> get() = _projects

    init {

        viewModelScope.launch {
            // Insert data into the database first
//            projectRepository.insertProject(
//                ProjectDetails(
//                    "suhdsu343s23", "Demo", "Demo des", "today",
//                    "10", false, "a"
//                )
//            )
//            projectRepository.insertProject(
//                ProjectDetails(
//                    "wud3hjdn11112", "Demo", "Demo des", "today",
//                    "10", false, "a"
//                )
//            )

            projectRepository.observeAllProjects().collect { projectList ->
                _projects.value = projectList
            }
        }
    }

    private val _cachedImagesList = MutableStateFlow<List<Image>>(emptyList())
    val cachedImagesList: StateFlow<List<Image>> = _cachedImagesList

    private val _cachedIntensityParts = MutableStateFlow<Int?>(null)
    val cachedIntensityParts: StateFlow<Int?> = _cachedIntensityParts

    fun cacheImageDetails(projectId: String) {
        viewModelScope.launch {
            val images = observerProjectImages(projectId).first()
            _cachedImagesList.value = images
        }
    }

    fun cacheIntensityParts(projectId: String) {
        viewModelScope.launch {
            val parts = observeNumberOfRfCountsByProjectId(projectId).first()
            _cachedIntensityParts.value = parts
        }
    }

    private val _selectedImageDetail = MutableStateFlow<Image?>(null)
    val selectedImageDetail: StateFlow<Image?> = _selectedImageDetail

    fun observeImageDetailByImageId(imageId: String) {
        viewModelScope.launch {
            imageRepository.observeImageById(imageId)
                .distinctUntilChanged()
                .collect { image ->
                    _selectedImageDetail.value = image
                }
        }
    }

    suspend fun updateImageDetailByImageId(image: Image) {
        imageRepository.updateImage(image)
    }


    fun insertProjectDetails(projectDetails: ProjectDetails) {
        viewModelScope.launch {
            projectRepository.insertProject(projectDetails)
        }
    }

    fun insertImage(image: Image) {
        viewModelScope.launch {
            imageRepository.insertImage(image)
        }
    }

    suspend fun getProjectCount(): Int {
        return projectRepository.getProjectCount()
    }

    suspend fun getProjectDetails(projectId: String): ProjectDetails? {
        return projectRepository.getProjectById(projectId)
    }

    fun observerProjectDetails(projectId: String): Flow<ProjectDetails?> {
        return projectRepository.observeProjectById(projectId)
    }

    fun observerProjectImages(projectId: String): Flow<List<Image>> {
        return imageRepository.observeAllImagesByProjectId(projectId)
    }

    suspend fun getProjectImages(projectId: String): List<Image> {
        return imageRepository.getAllImagesByProjectId(projectId)
    }

    suspend fun getNumberOfRfCountsByProjectId(projectId: String): Int {
        return projectRepository.getNumberOfRfCountsByProjectId(projectId)
    }

    fun observeNumberOfRfCountsByProjectId(projectId: String): Flow<Int> {
        return projectRepository.observeNumberOfRfCountsByProjectId(projectId)
            .distinctUntilChanged()
    }

    suspend fun updateNumberOfRfCountsByProjectId(projectId: String, rfCounts: Int) {
        projectRepository.updateNumberOfRfCountsByProjectId(projectId, rfCounts)
    }


}

package com.aican.tlcanalyzer.viewmodel.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.data.database.project.entities.ProjectDetails
import com.aican.tlcanalyzer.data.repository.project.ImageRepository
import com.aican.tlcanalyzer.data.repository.project.ProjectRepository
import com.aican.tlcanalyzer.data.service.project.ProjectService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectService: ProjectService,
    private val projectRepository: ProjectRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    /**
     * 🔹 Get the project type (split project or not)
     * @param projectId The unique identifier of the project
     * @return Boolean - `true` if the project is a split project, otherwise `false`
     */
    suspend fun getProjectType(projectId: String): Boolean {
        return projectRepository.getProjectType(projectId)
    }

    /**
     * 🔹 Observe all projects in the database as a Flow
     */
    val allProjects: Flow<List<ProjectDetails>> = projectRepository.observeAllProjects()

    // 🔹 Internal state to manage list of projects
    private val _projects: MutableStateFlow<List<ProjectDetails>> = MutableStateFlow(emptyList())
    val projects: StateFlow<List<ProjectDetails>> get() = _projects

    init {
        // 🔹 Collects the list of projects and updates state
        viewModelScope.launch {
            projectRepository.observeAllProjects().collect { projectList ->
                _projects.value = projectList
            }
        }
    }

    // 🔹 Caches the list of images in a project
    private val _cachedImagesList = MutableStateFlow<List<Image>>(emptyList())
    val cachedImagesList: StateFlow<List<Image>> = _cachedImagesList

    // 🔹 Caches the number of RF intensity parts for a project
    private val _cachedIntensityParts = MutableStateFlow<Int?>(null)
    val cachedIntensityParts: StateFlow<Int?> = _cachedIntensityParts

    /**
     * 🔹 Caches all image details for a given project
     * @param projectId The project ID whose images are to be cached
     */
    fun cacheImageDetails(projectId: String) {
        viewModelScope.launch {
            val images = observerProjectImages(projectId).first()
            _cachedImagesList.value = images
        }
    }

    /**
     * 🔹 Caches the number of RF intensity parts for a project
     * @param projectId The project ID
     */
    fun cacheIntensityParts(projectId: String) {
        viewModelScope.launch {
            val parts = observeNumberOfRfCountsByProjectId(projectId).first()
            _cachedIntensityParts.value = parts
        }
    }

    // 🔹 Holds the selected image details
    private val _selectedImageDetail = MutableStateFlow<Image?>(null)
    val selectedImageDetail: StateFlow<Image?> = _selectedImageDetail

    fun clearSelectedImage() {
        println("🧹 Clearing selected image in ViewModel")
        _selectedImageDetail.value = null
    }


    /**
     * 🔹 Observes image details by image ID and updates state
     * @param imageId The ID of the image to observe
     */
    fun observeImageDetailByImageId(imageId: String) {
        viewModelScope.launch {
            imageRepository.observeImageById(imageId)
                .distinctUntilChanged()
                .collect { image ->
                    _selectedImageDetail.value = image
                }
        }
    }

    /**
     * 🔹 Updates an image detail in the database
     * @param image The image entity with updated values
     */
    suspend fun updateImageDetailByImageId(image: Image) {
        imageRepository.updateImage(image)
    }

    /**
     * 🔹 Inserts a new project into the database
     * @param projectDetails The project details to insert
     */
    fun insertProjectDetails(projectDetails: ProjectDetails) {
        viewModelScope.launch {
            projectRepository.insertProject(projectDetails)
        }
    }

    /**
     * 🔹 Inserts an image into the database
     * @param image The image entity to insert
     */
    fun insertImage(image: Image) {
        viewModelScope.launch {
            imageRepository.insertImage(image)
        }
    }

    /**
     * 🔹 Retrieves the count of projects in the database
     * @return The total number of projects
     */
    suspend fun getProjectCount(): Int {
        return projectRepository.getProjectCount()
    }

    /**
     * 🔹 Retrieves project details by project ID
     * @param projectId The project ID
     * @return ProjectDetails object if found, otherwise `null`
     */
    suspend fun getProjectDetails(projectId: String): ProjectDetails? {
        return projectRepository.getProjectById(projectId)
    }

    /**
     * 🔹 Observes project details by project ID as a Flow
     * @param projectId The project ID
     * @return A Flow emitting project details
     */
    fun observerProjectDetails(projectId: String): Flow<ProjectDetails?> {
        return projectRepository.observeProjectById(projectId)
    }

    /**
     * 🔹 Observes all images for a specific project as a Flow
     * @param projectId The project ID
     * @return A Flow emitting a list of images
     */
    fun observerProjectImages(projectId: String): Flow<List<Image>> {
        return imageRepository.observeAllImagesByProjectId(projectId)
    }

    /**
     * 🔹 Gets all images associated with a project
     * @param projectId The project ID
     * @return A list of images for the project
     */
    suspend fun getProjectImages(projectId: String): List<Image> {
        return imageRepository.getAllImagesByProjectId(projectId)
    }

    /**
     * 🔹 Retrieves the number of RF counts for a project
     * @param projectId The project ID
     * @return The count of RF values
     */
    suspend fun getNumberOfRfCountsByProjectId(projectId: String): Int {
        return projectRepository.getNumberOfRfCountsByProjectId(projectId)
    }

    /**
     * 🔹 Observes the number of RF counts for a project as a Flow
     * @param projectId The project ID
     * @return A Flow emitting the number of RF counts
     */
    fun observeNumberOfRfCountsByProjectId(projectId: String): Flow<Int> {
        return projectRepository.observeNumberOfRfCountsByProjectId(projectId)
            .distinctUntilChanged()
    }

    /**
     * 🔹 Updates the number of RF counts for a project
     * @param projectId The project ID
     * @param rfCounts The new RF count value
     */
    suspend fun updateNumberOfRfCountsByProjectId(projectId: String, rfCounts: Int) {
        projectRepository.updateNumberOfRfCountsByProjectId(projectId, rfCounts)
    }


    suspend fun getAllMainImageByProjectId(projectId: String): List<Image> {
        return projectRepository.getAllMainImagesByProjectId(projectId)
    }

    suspend fun getAllSplitImageByProjectId(projectId: String): List<Image> {
        return projectRepository.getAllSplitImageByProjectId(projectId)
    }

}

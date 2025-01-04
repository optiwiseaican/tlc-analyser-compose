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
}

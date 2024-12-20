package com.aican.tlcanalyzer.data.database.project.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.data.database.project.entities.ProjectDetails

data class ProjectWithImages(
    @Embedded val projectDetails: ProjectDetails,  // The project details itself
    @Relation(
        parentColumn = "id",  // Parent column in the ProjectDetails table (Project ID)
        entityColumn = "projectId"  // The column in the Image table that links to the project
    )
    val images: List<Image>  // List of images associated with the project (Main and Split)
)

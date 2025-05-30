package com.aican.tlcanalyzer.data.database.project.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ProjectDetails")
data class ProjectDetails(
    @PrimaryKey val projectId: String,
    val projectName: String,
    val projectDescription: String,
    val timeStamp: String,
    val projectNumber: String,
    val mainImagePath: String,
    val imageSplitAvailable: Boolean = false,
    val projectImageId: String? = null,
    val sourceImageCount: Int = 0,
    val splitImageCount: Int = 0,
    val noOfRfCounts: Int = 100,
    val detectionType: String? = null
)

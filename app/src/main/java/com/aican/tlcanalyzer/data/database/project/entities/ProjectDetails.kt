package com.aican.tlcanalyzer.data.database.project.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ProjectDetails")
data class ProjectDetails(
    @PrimaryKey val id: String,
    val projectName: String,
    val projectDescription: String,
    val timeStamp: String,
    val projectNumber: String,
    val imageSplitAvailable: Boolean = false,
    val projectImageId: String? = null
)

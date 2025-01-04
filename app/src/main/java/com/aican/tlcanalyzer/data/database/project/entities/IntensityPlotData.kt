package com.aican.tlcanalyzer.data.database.project.entities

import androidx.room.*

@Entity(
    tableName = "IntensityPlotData",
    foreignKeys = [
        ForeignKey(
            entity = Image::class, // Reference the parent table
            parentColumns = ["imageId"],    // Primary key in the Image table
            childColumns = ["imageId"], // Foreign key in this table
            onDelete = ForeignKey.CASCADE // Cascade delete related rows
        )
    ],
    indices = [
        Index(value = ["imageId"]) // For faster lookups by imageId
    ]
)
data class IntensityPlotData(
    @PrimaryKey(autoGenerate = true) val intensityPlotId: Int = 0, // Auto-generated unique ID for each row
    val imageId: String,             // Foreign key linking to the Image table
    val rf: Double,                  // Retention factor (X-axis value in the plot)
    val intensity: Double            // Intensity or pixel value (Y-axis value in the plot)
)

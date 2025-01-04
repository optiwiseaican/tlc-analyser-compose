package com.aican.tlcanalyzer.data.database.project.entities

import androidx.room.*

@Entity(
    tableName = "ContourSpecificData",
    foreignKeys = [
        ForeignKey(
            entity = ContourData::class, // Reference the parent table
            parentColumns = ["contourId"],    // Primary key in the parent table
            childColumns = ["contourId"],     // Foreign key in this table
            onDelete = ForeignKey.CASCADE     // Cascade delete related rows
        ),
        ForeignKey(
            entity = Image::class, // Reference the parent table
            parentColumns = ["imageId"],    // Primary key in the Image table
            childColumns = ["imageId"], // Foreign key in this table
            onDelete = ForeignKey.CASCADE // Cascade delete related rows
        )
    ],
    indices = [
        Index(value = ["contourId"]), // For faster lookups on contourId
        Index(value = ["imageId"])    // For faster lookups on imageId
    ]
)

data class ContourSpecificData(
    @PrimaryKey(autoGenerate = true) val specificDataId: Int = 0, // Auto-generated unique ID for each row
    val contourId: String,           // Foreign key linking to ContourData
    val imageId: String,             // Foreign key linking to the Image table
    val rf: String,                  // Retention factor
    val rfTop: String,               // RF Top value
    val rfBottom: String,            // RF Bottom value
    val cv: String,                  // Coefficient of variation
    val area: String,                // Calculated area
    val areaPercent: String,         // Area percentage (optional, if relevant)
    val volume: String,              // Calculated volume
    val chemicalName: String? = null, // Optional: Name of the chemical associated with the contour
    val isSelected: Boolean = true,  // Whether the contour is selected
    val buttonColor: Int = 0         // Button color for UI representation
)

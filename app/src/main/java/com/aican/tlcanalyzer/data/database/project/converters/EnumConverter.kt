package com.aican.tlcanalyzer.data.database.project.converters

import androidx.room.TypeConverter
import com.aican.tlcanalyzer.data.database.project.entities.ContourType
import com.aican.tlcanalyzer.data.database.project.entities.ImageType

class EnumConverter {
    // ContourType Enum Conversion
    @TypeConverter
    fun fromContourType(value: ContourType): String = value.name

    @TypeConverter
    fun toContourType(value: String): ContourType = ContourType.valueOf(value)

    // Add converters for other enums here
    @TypeConverter
    fun fromImageType(value: ImageType): String = value.name

    @TypeConverter
    fun toImageType(value: String): ImageType = ImageType.valueOf(value)
}
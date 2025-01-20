package com.aican.tlcanalyzer.ui.pages.image_analysis.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aican.tlcanalyzer.data.database.project.entities.ContourData

@Composable
fun TableScreen(contourDataList: List<ContourData>) {

    // Column weights for the table
    val column1Weight = .3f // 30%
    val column2Weight = .7f // 70%

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        //    val name: String,
        //    val area: String,
        //    val volume: String,
        //    val rf: String,
        //    val rfTop: String,
        //    val rfBottom: String,
        //    val cv: String,
        //    val chemicalName: String,
        //    val type: ContourType

        // Header row
        Row(Modifier.background(Color.Gray)) {
            TableCell(text = "name", weight = column1Weight)
            TableCell(text = "rf", weight = column2Weight)
            TableCell(text = "cv", weight = column2Weight)
            TableCell(text = "area", weight = column2Weight)
            TableCell(text = "volume", weight = column2Weight)
            TableCell(text = "type", weight = column2Weight)
            TableCell(text = "chemicalName", weight = column2Weight)
        }

        // Table rows
        contourDataList.forEach { contour ->
            Row(Modifier.fillMaxWidth()) {
                TableCell(text = contour.name, weight = column1Weight)
                TableCell(text = contour.rf, weight = column2Weight)
                TableCell(text = contour.cv, weight = column2Weight)
                TableCell(text = contour.area, weight = column2Weight)
                TableCell(text = contour.volume, weight = column2Weight)
                TableCell(text = contour.type.toString(), weight = column2Weight)
                TableCell(text = contour.chemicalName, weight = column2Weight)
            }
        }
    }
}


@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float
) {
    Text(
        text = text,
        Modifier
            .border(1.dp, Color.Black)
            .weight(weight)
            .padding(8.dp)
    )
}
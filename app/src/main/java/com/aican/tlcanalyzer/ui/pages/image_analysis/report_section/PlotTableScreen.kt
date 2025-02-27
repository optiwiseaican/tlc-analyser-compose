package com.aican.tlcanalyzer.ui.pages.image_analysis.report_section

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aican.tlcanalyzer.ui.components.topbar_navigation.CustomTopBar
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.TableScreen
import com.aican.tlcanalyzer.viewmodel.project.ImageAnalysisViewModel

@Composable
fun PlotTableScreen(
    modifier: Modifier = Modifier,
    imageAnalysisViewModel: ImageAnalysisViewModel,
) {

    val contourDataList by imageAnalysisViewModel.allAutoGeneratedSpotsData.collectAsState()

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Table Plot",
                onBackClick = { /* Handle back navigation */ }
            )
        },
    ) { internalPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(internalPadding)
        ) {
            if (contourDataList.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(18.dp))

                    TableScreen(contourDataList)
                }

            } else {
                item {
                    Text(
                        text = "No table data available",
                        color = Color.Red
                    )
                }

            }
        }

    }


}
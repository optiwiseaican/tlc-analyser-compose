package com.aican.tlcanalyzer.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.aican.tlcanalyzer.ui.navigation.AppNavHost
import com.aican.tlcanalyzer.ui.navigation.ImageAnalysisNavHost
import com.aican.tlcanalyzer.ui.pages.image_analysis.AnalysisScreen
import com.aican.tlcanalyzer.viewmodel.project.ImageAnalysisViewModel
import com.aican.tlcanalyzer.viewmodel.project.IntensityChartViewModel
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat


@AndroidEntryPoint
class NewImageAnalysis : ComponentActivity() {

    private val viewModel: ProjectViewModel by viewModels()
    private val imageAnalysisViewModel: ImageAnalysisViewModel by viewModels()
    private val intensityChartViewModel: IntensityChartViewModel by viewModels()

    var projectId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        projectId = intent.getStringExtra("projectId") ?: ""

        OpenCVLoader.initLocal()

        setContent {

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    ImageAnalysisNavHost(
                        projectViewModel = viewModel,
                        projectId = projectId,
                        imageAnalysisViewModel = imageAnalysisViewModel,
                        intensityChartViewModel = intensityChartViewModel
                    )
                }
            }


        }


    }


}


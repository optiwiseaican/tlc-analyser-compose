package com.aican.tlcanalyzer.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.aican.tlcanalyzer.ui.navigation.ImageAnalysisNavHost
import com.aican.tlcanalyzer.ui.pages.split_image_section.SplitImageScreen
import com.aican.tlcanalyzer.viewmodel.project.ImageAnalysisViewModel
import com.aican.tlcanalyzer.viewmodel.project.IntensityChartViewModel
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader

@AndroidEntryPoint
class NewImageAnalysis : ComponentActivity() {

    private val viewModel: ProjectViewModel by viewModels()
    private val imageAnalysisViewModel: ImageAnalysisViewModel by viewModels()
    private val intensityChartViewModel: IntensityChartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val projectId = intent.getStringExtra("projectId") ?: ""
        val imageId = intent.getStringExtra("imageId") ?: ""

        OpenCVLoader.initLocal()

        setContent {

            val navController: NavHostController = rememberNavController()


            // ✅ State to store project type
            var isSplitProject by remember { mutableStateOf<Boolean?>(null) }
            val coroutineScope = rememberCoroutineScope()

            // ✅ Fetch project type when UI is first composed
            LaunchedEffect(projectId) {
                coroutineScope.launch {
                    isSplitProject = viewModel.getProjectType(projectId)
                }
            }

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {

                    if (isSplitProject != null) {

                        ImageAnalysisNavHost(
                            navController = navController,
                            projectViewModel = viewModel,
                            imageAnalysisViewModel = imageAnalysisViewModel,
                            intensityChartViewModel = intensityChartViewModel,
                            projectId = projectId,
                            imageId = imageId,
                            isSplitProject = isSplitProject!!
                        )
                    }

                }
            }
        }
    }
}

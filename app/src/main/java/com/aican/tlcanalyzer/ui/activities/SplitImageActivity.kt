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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.aican.tlcanalyzer.ui.activities.ui.theme.TLCAnalyzerTheme
import com.aican.tlcanalyzer.ui.navigation.ImageAnalysisNavHost
import com.aican.tlcanalyzer.ui.navigation.SplitImageNavHost
import com.aican.tlcanalyzer.viewmodel.project.ImageAnalysisViewModel
import com.aican.tlcanalyzer.viewmodel.project.IntensityChartViewModel
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplitImageActivity : ComponentActivity() {


    private val viewModel: ProjectViewModel by viewModels()
    private val imageAnalysisViewModel: ImageAnalysisViewModel by viewModels()
    private val intensityChartViewModel: IntensityChartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val projectId = intent.getStringExtra("projectId") ?: ""

        setContent {
            val navController: NavHostController = rememberNavController()

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {


                        SplitImageNavHost(
                            navController = navController,
                            projectViewModel = viewModel,
                            imageAnalysisViewModel = imageAnalysisViewModel,
                            intensityChartViewModel = intensityChartViewModel,
                            projectId = projectId,
                        )

                }
            }
            }
        }

}

package com.aican.tlcanalyzer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aican.tlcanalyzer.ui.pages.image_analysis.AnalysisScreen
import com.aican.tlcanalyzer.ui.pages.settings_pages.ProjectSettingsScreen
import com.aican.tlcanalyzer.viewmodel.project.ImageAnalysisViewModel
import com.aican.tlcanalyzer.viewmodel.project.IntensityChartViewModel
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel


@Composable
fun ImageAnalysisNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    projectViewModel: ProjectViewModel,
    imageAnalysisViewModel: ImageAnalysisViewModel,
    intensityChartViewModel: IntensityChartViewModel,
    projectId: String
) {

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = ImageAnalysisRoute.ROUTE_IMAGE_ANALYSIS
    ) {

        composable<ImageAnalysisRoute.ROUTE_IMAGE_ANALYSIS> {
            AnalysisScreen(
                projectViewModel = projectViewModel,
                imageAnalysisViewModel = imageAnalysisViewModel,
                intensityChartViewModel = intensityChartViewModel,
                projectId = projectId
            ) { routeDestination ->
                if (routeDestination == "image_analysis_settings") {
                    navController.navigate(ImageAnalysisRoute.ROUTE_IMAGE_ANALYSIS_SETTINGS)

                }
            }
        }

        composable<ImageAnalysisRoute.ROUTE_IMAGE_ANALYSIS_SETTINGS> {
            ProjectSettingsScreen(projectId = projectId, projectViewModel = projectViewModel)
        }


    }


}
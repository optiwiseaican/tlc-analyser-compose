package com.aican.tlcanalyzer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aican.tlcanalyzer.ui.pages.image_analysis.AnalysisScreen
import com.aican.tlcanalyzer.ui.pages.image_analysis.report_section.IntensityPlotScreen
import com.aican.tlcanalyzer.ui.pages.image_analysis.report_section.PlotTableScreen
import com.aican.tlcanalyzer.ui.pages.image_analysis.report_section.ReportScreen
import com.aican.tlcanalyzer.ui.pages.image_analysis.report_section.VolumePlotScreen
import com.aican.tlcanalyzer.ui.pages.settings_pages.CropScreen
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
                projectId = projectId,
                onIntensityPlot = {
                    navController.navigate(ImageAnalysisRoute.ROUTE_INTENSITY_PLOT) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }, onNavigate = { routeDestination ->
                    if (routeDestination == "image_analysis_settings") {
                        navController.navigate(ImageAnalysisRoute.ROUTE_IMAGE_ANALYSIS_SETTINGS) {
                            launchSingleTop = true
                            restoreState = true
                        }

                    }
                    if (routeDestination == "crop_screen") {

                        navController.navigate(ImageAnalysisRoute.ROUTE_CROP_SETTINGS)

                    }
                    if (routeDestination == "IntensityPlotScreen") {
                        navController.navigate(ImageAnalysisRoute.ROUTE_INTENSITY_PLOT) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }

                    if (routeDestination == "VolumePlotScreen") {
                        navController.navigate(ImageAnalysisRoute.ROUTE_VOLUME_PLOT) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    if (routeDestination == "PlotTableScreen") {
                        navController.navigate(ImageAnalysisRoute.ROUTE_TABLE_PLOT) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    if (routeDestination == "ReportScreen") {
                        navController.navigate(ImageAnalysisRoute.ROUTE_REPORT) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                })
        }

        composable<ImageAnalysisRoute.ROUTE_IMAGE_ANALYSIS_SETTINGS> {
            ProjectSettingsScreen(projectId = projectId, projectViewModel = projectViewModel)
        }

        composable<ImageAnalysisRoute.ROUTE_CROP_SETTINGS> {
            CropScreen()

        }

        composable<ImageAnalysisRoute.ROUTE_INTENSITY_PLOT> {
            IntensityPlotScreen(
                imageAnalysisViewModel = imageAnalysisViewModel,
                intensityChartViewModel = intensityChartViewModel,
                projectViewModel = projectViewModel
            )
        }


        composable<ImageAnalysisRoute.ROUTE_VOLUME_PLOT> {
            VolumePlotScreen(
                imageAnalysisViewModel = imageAnalysisViewModel,
            )
        }


        composable<ImageAnalysisRoute.ROUTE_TABLE_PLOT> {
            PlotTableScreen(
                imageAnalysisViewModel = imageAnalysisViewModel,
            )
        }



        composable<ImageAnalysisRoute.ROUTE_REPORT> {
            ReportScreen(
            ) {

            }
        }


    }


}
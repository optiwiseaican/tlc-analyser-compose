package com.aican.tlcanalyzer.ui.navigation

import android.content.Intent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aican.tlcanalyzer.data.database.project.entities.ProjectDetails
import com.aican.tlcanalyzer.ui.activities.NewCameraActivity
import com.aican.tlcanalyzer.ui.activities.NewImageAnalysis
import com.aican.tlcanalyzer.ui.pages.split_image_section.SplitImageScreen
import com.aican.tlcanalyzer.viewmodel.project.ImageAnalysisViewModel
import com.aican.tlcanalyzer.viewmodel.project.IntensityChartViewModel
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel

@Composable
fun SplitImageNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    projectViewModel: ProjectViewModel,
    imageAnalysisViewModel: ImageAnalysisViewModel,
    intensityChartViewModel: IntensityChartViewModel,
    projectId: String,
    projectDescription: String,
    projectName: String,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = SplitImageRoute.ROUTE_SPLIT_IMAGE
    ) {


        composable<SplitImageRoute.ROUTE_SPLIT_IMAGE>(
            enterTransition = { fadeIn(tween(1000)) },
            exitTransition = { fadeOut(tween(700)) },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(700)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(700)
                )
            }

        ) {
            val context = LocalContext.current
            SplitImageScreen(
                projectViewModel = projectViewModel,
                projectId = projectId,
                projectName = projectName,
                navController = navController,
                onBackClick = { navController.popBackStack() },
                onSettingsClick = { },
                onAddMainImageClick = {
                    val intent = Intent(context, NewCameraActivity::class.java)
                    intent.putExtra("projectId", projectId)
                    intent.putExtra("mainImageAdding", "true")
                    intent.putExtra("projectName", projectName)
                    intent.putExtra("projectDescription", projectDescription)
                    context.startActivity(intent)
                },
                onMainImageClick = { image ->
                    var intent = Intent(context, NewImageAnalysis::class.java)
                    intent.putExtra("projectId", projectId)
                    intent.putExtra("imageId", image.imageId)
                    context.startActivity(intent)
//                    navController.navigate("${ImageAnalysisRoute.ROUTE_IMAGE_ANALYSIS}/$projectId?imageId=${image.imageId}")
                },
                onSplitImageClick = { image ->
                    var intent = Intent(context, NewImageAnalysis::class.java)
                    intent.putExtra("projectId", projectId)
                    intent.putExtra("imageId", image.imageId)
                    context.startActivity(intent)
//                    navController.navigate("${ImageAnalysisRoute.ROUTE_IMAGE_ANALYSIS}/$projectId?imageId=${image.imageId}")
                }
            )
        }
    }


}
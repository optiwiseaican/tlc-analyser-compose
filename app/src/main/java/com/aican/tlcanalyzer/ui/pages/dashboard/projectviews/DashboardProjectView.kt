package com.aican.tlcanalyzer.ui.pages.dashboard.projectviews

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.aican.tlcanalyzer.data.database.project.entities.ProjectDetails
import com.aican.tlcanalyzer.ui.pages.dashboard.projectviews.components.ProjectTiles
import com.aican.tlcanalyzer.ui.pages.dashboard.projectviews.components.TempProjectTiles

@Composable
fun DashboardProjectView(
    projects: List<ProjectDetails>,
) {

    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    LazyVerticalGrid(
        columns = if (isLandscape) GridCells.Fixed(2) else GridCells.Fixed(1),
        modifier = Modifier
    ) {
        items(items = projects) { project ->
            ProjectTiles(project = project)
//            ProjectTiles(project = project)
            Spacer(modifier = Modifier.height(15.dp))
        }
    }

}
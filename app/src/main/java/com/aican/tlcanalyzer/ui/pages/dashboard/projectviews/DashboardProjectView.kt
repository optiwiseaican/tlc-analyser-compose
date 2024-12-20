package com.aican.tlcanalyzer.ui.pages.dashboard.projectviews

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aican.tlcanalyzer.ui.pages.dashboard.projectviews.components.ProjectTiles

@Composable
fun DashboardProjectView(modifier: Modifier = Modifier) {
    Column {
        ProjectTiles()
        Spacer(modifier = Modifier.height(5.dp))

        ProjectTiles()
        Spacer(modifier = Modifier.height(5.dp))
        ProjectTiles()
        Spacer(modifier = Modifier.height(5.dp))
        ProjectTiles()
        Spacer(modifier = Modifier.height(5.dp))
    }
}
package com.aican.tlcanalyzer.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.databinding.ActivityNewImageAnalysisBinding
import com.aican.tlcanalyzer.ui.pages.image_analysis.AnalysisScreen
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewImageAnalysis : AppCompatActivity() {

    private val viewModel: ProjectViewModel by viewModels()
    var projectId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        projectId = intent.getStringExtra("projectId") ?: ""


        setContent {
            AnalysisScreen(projectViewModel = viewModel, projectId = projectId)
        }


    }


}
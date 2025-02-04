package com.aican.tlcanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.aican.tlcanalyzer.ui.navigation.AppNavHost
import com.aican.tlcanalyzer.utils.AppUtils
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

//    @Inject
//    lateinit var contourRepository: ContourRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        CoroutineScope(Dispatchers.IO).launch {
//            contourRepository.nukeContourDataTable()
//            contourRepository.nukeContourPointsTable()
//        }
        val exportDir = File(getExternalFilesDir(null).toString() + "/" + "All PDF Files")

        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    AppNavHost()
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        AppUtils.MAIN_IMAGE_COUNT = 0
    }
}

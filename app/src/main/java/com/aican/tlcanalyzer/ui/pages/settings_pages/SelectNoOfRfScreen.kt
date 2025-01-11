package com.aican.tlcanalyzer.ui.pages.settings_pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun ProjectSettingsScreen(projectId: String, projectViewModel: ProjectViewModel) {

    val numberOfIntensityParts by projectViewModel.observeNumberOfRfCountsByProjectId(projectId = projectId)
        .collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Back Button",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { /* Handle back action */ }
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Project Settings",
                modifier = Modifier.weight(1f),
                style = TextStyle(
                    color = Color.Blue,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            )
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // Select Number of Intensity Parts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select no. intensity parts",
                    modifier = Modifier.weight(2f),
                    style = TextStyle(
                        color = Color(0xFF007BFF), // Replace with @color/aican_blue
                        fontSize = 18.sp
                    )
                )

                if (numberOfIntensityParts != null) {
                    // Spinner (Dropdown)
                    var expanded by remember { mutableStateOf(false) }
                    var selectedOption by remember { mutableStateOf(numberOfIntensityParts.toString()) }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .border(
                                width = 1.dp,
                                color = Color.Gray, // Replace with custom drawable if needed
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { expanded = true }
                    ) {
                        Text(
                            text = selectedOption,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .align(Alignment.CenterStart)
                        )


                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }

                        ) {
                            listOf(
                                "100",
                                "500",
                                "1000",
                                "2000",
                                "5000",
                                "10000"
                            ).forEach { option ->
                                DropdownMenuItem(
                                    onClick = {
                                        projectViewModel.apply {
                                            viewModelScope.launch {
                                                projectViewModel.updateNumberOfRfCountsByProjectId(
                                                    projectId,
                                                    option.toInt()
                                                )
                                            }
                                        }
                                        selectedOption = option
                                        expanded = false
                                    },

                                    text = { Text(option) }
                                )


                            }
                        }
                    }
                }
            }

            Divider(color = Color.Gray, thickness = 1.dp)

            Spacer(modifier = Modifier.height(5.dp))

            // Warning Text
            Text(
                text = "If you change this, previous settings will be reset.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                color = Color.Red, // Replace with @color/ai_red
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(5.dp))

            Divider(color = Color.Gray, thickness = 1.dp)
        }
    }
}

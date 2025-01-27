package com.aican.tlcanalyzer.ui.pages.image_analysis.report_section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aican.tlcanalyzer.ui.components.topbar_navigation.CustomTopBar

@Composable
fun ReportScreen(modifier: Modifier = Modifier, onGenerateReportClick: () -> Unit) {
    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Generate Report",
                showBackButton = true,
                onBackClick = { /* Handle back navigation */ }
            )
        }
    ) { internalPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(internalPadding)
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "Get reports of",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Options List
            val options = listOf(
                "Original image",
                "Detected contours image",
                "Contour's detailed table",
                "Intensity Plot",
                "Volume Plot",
                "Select ROI"
            )

            val selectedOptions = remember { mutableStateListOf<String>() }

            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedOptions.contains(option),
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                selectedOptions.add(option)
                            } else {
                                selectedOptions.remove(option)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Generate Report Button
            Button(
                onClick = { onGenerateReportClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = "Generate Report")
            }

            // ROI Data Section
            Text(
                text = "ROI Data",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .padding(8.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // ROI Data Content (Placeholder)
                Text(
                    text = "ROI Data Content",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

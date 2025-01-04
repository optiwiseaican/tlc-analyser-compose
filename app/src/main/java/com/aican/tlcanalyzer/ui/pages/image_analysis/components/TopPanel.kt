package com.aican.tlcanalyzer.ui.pages.image_analysis.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TopPanel(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* Handle Back */ }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Button(onClick = { /* Handle Save */ }) {
            Text(text = "Save")
        }
        IconButton(onClick = { /* Handle Settings */ }) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
        IconButton(onClick = { /* Handle Crop Again */ }) {
            Icon(Icons.Default.Create, contentDescription = "Crop Again", tint = Color.Red)
        }
    }
}

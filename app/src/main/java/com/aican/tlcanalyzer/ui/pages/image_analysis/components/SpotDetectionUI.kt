package com.aican.tlcanalyzer.ui.pages.image_analysis.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SpotDetectionUI(thresholdVal: Int, numberOfSpots: Int, onGenerateSpots: (Int, Int) -> Unit) {
    var currentThreshold by remember { mutableIntStateOf(thresholdVal) }

    var currentNumberOfSpots by remember {
        mutableIntStateOf(numberOfSpots)
    }
    // Synchronize currentThreshold with thresholdVal whenever it changes
    LaunchedEffect(thresholdVal) {
        currentThreshold = thresholdVal

    }
    LaunchedEffect(numberOfSpots) {
        currentNumberOfSpots = numberOfSpots
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Spot Detection",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Start)
        )

        SpotSlider(
            label = "Threshold",
            value = currentThreshold.toFloat(),
            onValueChange = { newValue -> currentThreshold = newValue.toInt() },
            max = 255f
        )

        SpotSlider(
            label = "No of Spots",
            value = currentNumberOfSpots.toFloat(),
            onValueChange = { newValue -> currentNumberOfSpots = newValue.toInt() },
            max = 100f
        )

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(text = "Generate Spots",
                onClick = { onGenerateSpots(currentThreshold, currentNumberOfSpots) })
            ActionButton(text = "Add Spot", onClick = { /* Handle Add Spot */ })
        };
    }
}

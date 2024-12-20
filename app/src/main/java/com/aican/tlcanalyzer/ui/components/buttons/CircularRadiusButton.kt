package com.aican.tlcanalyzer.ui.components.buttons

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

@Composable
fun CircularRadiusButton(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = Color.White,
    backgroundColor: Color = Color.Black,
    onClick: () -> Unit
) {

    Button(
        modifier = modifier
            .clip(CircleShape),
        onClick = { onClick.invoke() },
        elevation = null,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        )
    ) {

        Text(text = text)

    }
}
package com.aican.tlcanalyzer.ui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun OutlinedRadiusButton(
    modifier: Modifier = Modifier,
    text: String,
    borderColor: Color = Color.Black,
    textColor: Color = Color.Black,
    borderWidth: Dp = 1.dp,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        onClick = { onClick.invoke() },
        border = BorderStroke(borderWidth, borderColor),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
    ) {
        Text(text = text)
    }
}
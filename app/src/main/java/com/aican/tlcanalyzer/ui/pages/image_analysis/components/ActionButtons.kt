package com.aican.tlcanalyzer.ui.pages.image_analysis.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    containerColor: Color = Color.Blue,
    contentColor: Color = Color.White,
    padding: Int = 8,
    fontSize: Int = 14,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Button(
        onClick = onClick,
        modifier = modifier.padding(horizontal = padding.dp, vertical = (padding / 2).dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.LightGray
        ),
        enabled = enabled
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = fontSize.sp,
                fontWeight = fontWeight
            )
        )
    }
}

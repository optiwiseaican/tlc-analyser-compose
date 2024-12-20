package com.aican.tlcanalyzer.ui.components.textfields

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RoundedEditTextField(
    modifier: Modifier = Modifier,
    text: String,
    hint: String = "",
    onTextChanged: (String) -> Unit
) {
    Box(modifier = modifier
        .fillMaxWidth()) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = {
                onTextChanged(it)
            },
            placeholder = {
                Text(hint)
            },
            textStyle = TextStyle(fontSize = 16.sp),
            shape = RoundedCornerShape(35.dp),
        )
    }
}
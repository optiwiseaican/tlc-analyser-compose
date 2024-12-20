package com.aican.tlcanalyzer.ui.pages.common_components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aican.tlcanalyzer.ui.components.textfields.RoundedEditTextField



@Composable
fun EditTextFieldWithLabel(
    modifier: Modifier = Modifier,
    text: String,
    hint: String = "",
    label: String = "",
    onTextChanged: (String) -> Unit
) {

    Column(
        modifier = Modifier.padding(
            top = 0.dp, bottom = 0.dp, start = 15.dp,
            end = 15.dp
        )
    ) {

        Text(text = label)
        Spacer(modifier = Modifier.height(4.dp))
        RoundedEditTextField(
            text = text, hint = hint, modifier = Modifier
        ) {
            onTextChanged.invoke(it)

        }
    }

}
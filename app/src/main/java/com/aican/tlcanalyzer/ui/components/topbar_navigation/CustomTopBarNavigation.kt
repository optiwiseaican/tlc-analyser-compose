package com.aican.tlcanalyzer.ui.components.topbar_navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.utils.AppUtils

@Composable
fun CustomTopBarNavigation(
    modifier: Modifier = Modifier,
    titleText: String = AppUtils.APP_NAME,
    textColor: Color = Color.Black,
    textSize: TextUnit = 18.sp,
    drawable: Int = R.drawable.back,
    onClick: () -> Unit
) {
    Column {
        Row {

            Spacer(modifier = Modifier.width(10.dp))

            Image(
                modifier = Modifier
                    .width(50.dp)
                    .height(40.dp)
                    .padding(5.dp)
                    .clickable {
                        onClick.invoke()
                    },
                painter = painterResource(id = drawable),
                contentDescription = "Back Button",
            )
            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = titleText, style = TextStyle(
                    fontSize = textSize,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.align(Alignment.CenterVertically)
            )


        }
        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider(color = Color.Black, thickness = 1.dp)
        Spacer(modifier = Modifier.height(0.dp))
    }
}
package com.aican.tlcanalyzer.ui.pages.dashboard.projectviews.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aican.tlcanalyzer.R

@Composable
fun ProjectTiles(modifier: Modifier = Modifier) {
    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        border = BorderStroke(1.dp, Color.LightGray),
        elevation = CardDefaults.cardElevation(
            3.dp
        ),
        modifier = modifier
            .wrapContentHeight()
            .padding(start = 15.dp, end = 15.dp)
            .fillMaxWidth()
    ) {

        Row {
            Image(
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
                    .padding(start = 10.dp, end = 5.dp, top = 5.dp, bottom = 5.dp)
                    .clickable {
                    },
                painter = painterResource(id = R.drawable.tlc_img),
                contentDescription = "Back Button",
            )
            Column(modifier = Modifier.align(Alignment.CenterVertically)) {


                Text(
                    text = "Project 1",
                    modifier = Modifier
                        .padding(top = 0.dp, start = 16.dp),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "26/08/2002 07:50 PM",
                    modifier = Modifier
                        .padding(top = 5.dp, start = 16.dp),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 12.sp)

                )

                Text(
                    text = "project_id",
                    modifier = Modifier
                        .padding(top = 5.dp, start = 16.dp),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 12.sp)

                )

            }
        }

    }
}
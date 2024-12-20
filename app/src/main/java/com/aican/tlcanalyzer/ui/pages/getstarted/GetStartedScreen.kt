package com.aican.tlcanalyzer.ui.pages.getstarted

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aican.tlcanalyzer.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GetStartedScreen(getStartedOnClick: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = {
            Box(modifier = Modifier.fillMaxSize()) {

                Column {
                    Card(

                        colors = CardDefaults.cardColors(
                            containerColor = Color.Blue,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        shape = RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp),
                        onClick = {

                        },
                    ) {


                    }

                    Image(
                        painter = painterResource(id = R.drawable.tlc_img),
                        contentDescription = "TLC Image",

                        modifier = Modifier
                            .size(150.dp)
                            .offset(y = (-60).dp)
                            .align(Alignment.CenterHorizontally)
                            .background(
                                Color.Yellow,
                                // rounded corner to match with the OutlinedTextField
                                shape = RoundedCornerShape(500.dp)
                            )
                            .clip(RoundedCornerShape(topEnd = 8.dp, topStart = 8.dp))
                            .padding(20.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 50.dp)
                    ) {
                        Text(
                            text = "Simplify your TLC analysis!",
                            color = Color.Blue,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(top = 50.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Introducing the TLC Spot Analyzer - Your Essential Tool for Accurate TLC Analysis.",
                            color = Color.Blue,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { getStartedOnClick.invoke() },
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                                .align(Alignment.CenterHorizontally)

                        ) {
                            Text(
                                text = "Get Started Now",
                                maxLines = 1,
                                modifier = Modifier.weight(1f),
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_arrow_right_alt_24),
                                tint = Color.White,

                                contentDescription = null
                            )
                        }
                    }

                }


            }
        }
    )
}

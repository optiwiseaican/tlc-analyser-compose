package com.aican.tlcanalyzer.ui.pages.dashboard.projectviews.components

import android.content.Intent
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
import androidx.compose.material3.Button
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.data.database.project.entities.ProjectDetails
import com.aican.tlcanalyzer.ui.activities.NewCameraActivity
import com.aican.tlcanalyzer.ui.activities.NewImageAnalysis
import com.aican.tlcanalyzer.ui.activities.SplitImageActivity
import com.aican.tlcanalyzer.utils.AppUtils
import java.io.File

@Composable
fun TempProjectTiles(
    modifier: Modifier = Modifier,
    project: ProjectDetails,
    onNavigate: (String) -> Unit
) {
    Button(onClick = { onNavigate.invoke("") }) {

    }
}

@Composable
fun ProjectTiles(modifier: Modifier = Modifier, project: ProjectDetails) {

    val context = LocalContext.current

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
            .clickable {
                if (project.imageSplitAvailable) {
                    val intent = Intent(context, SplitImageActivity::class.java).apply {
                        putExtra("projectId", project.projectId)
                    }
                    context.startActivity(intent)
                } else {
                    val intent = Intent(context, NewImageAnalysis::class.java).apply {
                        putExtra("projectId", project.projectId)
                    }
                    context.startActivity(intent)
                }
            }
    ) {

        Row {
            //load image from file

            val imageFile = File(project.mainImagePath)
            if (imageFile.exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageFile)
                        .build(),
                    contentDescription = "icon",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp)
                        .padding(start = 10.dp, end = 5.dp, top = 5.dp, bottom = 5.dp)

                )


            } else {
                Image(
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp)
                        .padding(start = 10.dp, end = 5.dp, top = 5.dp, bottom = 5.dp)
                        .clickable {
                        },
                    painter = painterResource(id = R.drawable.baseline_warning_24),
                    contentDescription = "Back Button",
                )
            }

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {


                Text(
                    text = project.projectName,
                    modifier = Modifier
                        .padding(top = 0.dp, start = 16.dp),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = AppUtils.decodeTimestamp(project.timeStamp),
                    modifier = Modifier
                        .padding(top = 5.dp, start = 16.dp),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 12.sp)

                )

                Text(
                    text = project.projectId,
                    modifier = Modifier
                        .padding(top = 5.dp, start = 16.dp),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 8.sp)

                )

            }
        }

    }
}
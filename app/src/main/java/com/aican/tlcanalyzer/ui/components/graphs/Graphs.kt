package com.aican.tlcanalyzer.ui.components.graphs

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aican.tlcanalyzer.domain.model.graphs.GraphPoint

@Composable
fun ZoomableGraph(
    modifier: Modifier = Modifier,
    points: List<GraphPoint>,
    paddingSpace: Dp = 16.dp,
    verticalStep: Int = 50 // Step interval for Y-axis
) {
    // Variables to track zoom, pan, and position
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // Get min and max for X and Y values for scaling
    val xMin = points.minOfOrNull { it.x } ?: 0f
    val xMax = points.maxOfOrNull { it.x } ?: 1f
    val yMin = points.minOfOrNull { it.y } ?: 0f
    val yMax = points.maxOfOrNull { it.y } ?: 1f

    // Handle gestures: Pinch-to-zoom, drag, and double-tap zoom
    val gestureModifier = Modifier
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                scale = (scale * zoom).coerceIn(1f, 500f) // Limit zoom scale between 1x and 5x
                offsetX += pan.x
                offsetY += pan.y
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = {
                    scale = if (scale == 1f) 2f else 1f // Toggle between 1x and 2x zoom on double-tap
                }
            )
        }

    Box(
        modifier = modifier
            .background(Color.White)
            .then(gestureModifier)
    ) {
        Graph(
            modifier = Modifier.fillMaxSize(),
            points = points,
            xMin = xMin,
            xMax = xMax,
            yMin = yMin,
            yMax = yMax,
            scale = scale,
            offsetX = offsetX,
            offsetY = offsetY,
            paddingSpace = paddingSpace,
            verticalStep = verticalStep
        )
    }
}

@Composable
fun Graph(
    modifier: Modifier = Modifier,
    points: List<GraphPoint>,
    xMin: Float,
    xMax: Float,
    yMin: Float,
    yMax: Float,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    paddingSpace: Dp = 16.dp,
    verticalStep: Int = 50
) {
    Box(
        modifier = modifier
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Calculate spacing for X and Y axes
            val width = size.width - 2 * paddingSpace.toPx()
            val height = size.height - 2 * paddingSpace.toPx()
            val xAxisSpacing = width / (xMax - xMin)
            val yAxisSpacing = height / (yMax - yMin)

            // Apply scaling and translation for the grid and axes
            val scaledXSpacing = xAxisSpacing * scale
            val scaledYSpacing = yAxisSpacing * scale

            // Draw X-axis grid and labels
            for (i in 0..(xMax - xMin).toInt()) {
                val x = paddingSpace.toPx() + i * scaledXSpacing + offsetX
                if (x in paddingSpace.toPx()..(size.width - paddingSpace.toPx())) {
                    // Vertical grid line
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(x, paddingSpace.toPx()),
                        end = Offset(x, size.height - paddingSpace.toPx()),
                        strokeWidth = 1f
                    )

                    // X-axis label
                    drawContext.canvas.nativeCanvas.drawText(
                        (xMin + i).toString(),
                        x,
                        size.height - paddingSpace.toPx() + 20,
                        Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 30f / scale
                            textAlign = Paint.Align.CENTER
                        }
                    )
                }
            }

            // Draw Y-axis grid and labels
            for (i in 0..(yMax - yMin).toInt() step verticalStep) {
                val y = size.height - paddingSpace.toPx() - i * scaledYSpacing + offsetY
                if (y in paddingSpace.toPx()..(size.height - paddingSpace.toPx())) {
                    // Horizontal grid line
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(paddingSpace.toPx(), y),
                        end = Offset(size.width - paddingSpace.toPx(), y),
                        strokeWidth = 1f
                    )

                    // Y-axis label
                    drawContext.canvas.nativeCanvas.drawText(
                        (yMin + i).toString(),
                        paddingSpace.toPx() - 20,
                        y + 10,
                        Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 30f / scale
                            textAlign = Paint.Align.RIGHT
                        }
                    )
                }
            }

            // Map data points to coordinates with scaling
            val pointsOffset = points.map { point ->
                Offset(
                    x = paddingSpace.toPx() + (point.x - xMin) / (xMax - xMin) * width * scale + offsetX,
                    y = size.height - paddingSpace.toPx() - (point.y - yMin) / (yMax - yMin) * height * scale + offsetY
                )
            }

            // Draw Bézier curve
            val path = Path().apply {
                moveTo(pointsOffset.first().x, pointsOffset.first().y)
                for (i in 1 until pointsOffset.size) {
                    val p0 = pointsOffset[i - 1]
                    val p1 = pointsOffset[i]
                    val controlPoint1 = Offset((p0.x + p1.x) / 2, p0.y)
                    val controlPoint2 = Offset((p0.x + p1.x) / 2, p1.y)
                    cubicTo(
                        controlPoint1.x, controlPoint1.y,
                        controlPoint2.x, controlPoint2.y,
                        p1.x, p1.y
                    )
                }
            }

            // Draw the path for Bézier curve
            drawPath(
                path = path,
                color = Color.Red,
                style = Stroke(width = 3f)
            )

            // Draw data points
            pointsOffset.forEach { point ->
                drawCircle(
                    color = Color.Blue,
                    radius = 8f / scale,
                    center = point
                )
            }
        }
    }
}

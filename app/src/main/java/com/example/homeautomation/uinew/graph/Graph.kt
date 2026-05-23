package com.example.homeautomation.uinew.graph

import android.graphics.Paint
import java.util.Locale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

private val GraphCardColor = Color(0x221E88E5)
private val AxisColor = Color.White.copy(alpha = 0.8f)
private val GridColor = Color.White.copy(alpha = 0.18f)
private val LineColor = Color.White
private val PointColor = Color(0xFF2ECC71)

@Composable
fun SmoothTemperatureGraph(
    history: List<Float>,
    modifier: Modifier = Modifier,
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    if (history.size < 2) {
        Text(
            text = "Need more temperature data",
            color = Color.White,
            fontSize = 18.sp,
        )
        return
    }

    Box(
        modifier = modifier
            .background(
                color = GraphCardColor,
                shape = RoundedCornerShape(22.dp),
            )
            .padding(12.dp),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(history) {
                    detectTapGestures { offset ->
                        selectedIndex = findNearestPointIndex(
                            tapX = offset.x,
                            historySize = history.size,
                            canvasWidth = size.width.toFloat(),
                        )
                    }
                }
                .pointerInput(history) {
                    detectDragGestures { change, _ ->
                        selectedIndex = findNearestPointIndex(
                            tapX = change.position.x,
                            historySize = history.size,
                            canvasWidth = size.width.toFloat(),
                        )
                    }
                },
        ) {
            val leftPadding = 65f
            val rightPadding = 30f
            val topPadding = 45f
            val bottomPadding = 60f

            val graphWidth = size.width - leftPadding - rightPadding
            val graphHeight = size.height - topPadding - bottomPadding

            val maxValue = history.maxOrNull() ?: 0f
            val minValue = history.minOrNull() ?: 0f
            val range = maxValue - minValue

            val labelPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 26f
                alpha = 180
            }

            val valuePaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 26f
                isFakeBoldText = true
            }

            val selectedPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 30f
                isFakeBoldText = true
            }

            val points = history.mapIndexed { index, value ->
                val x = leftPadding + index * graphWidth / (history.lastIndex)

                val y = if (range == 0f) {
                    topPadding + graphHeight / 2
                } else {
                    topPadding + graphHeight - ((value - minValue) / range * graphHeight)
                }

                Offset(x, y)
            }

            val smoothPath = createSmoothPath(points)

            val areaPath = Path().apply {
                moveTo(points.first().x, topPadding + graphHeight)
                addPath(smoothPath)
                lineTo(points.last().x, topPadding + graphHeight)
                close()
            }

            // Grid lines and Y-axis values
            val ySteps = 4

            for (i in 0..ySteps) {
                val y = topPadding + i * graphHeight / ySteps
                val value = maxValue - i * range / ySteps

                drawLine(
                    color = GridColor,
                    start = Offset(leftPadding, y),
                    end = Offset(leftPadding + graphWidth, y),
                    strokeWidth = 1.5f,
                )

                drawContext.canvas.nativeCanvas.drawText(
                    "${value.roundToInt()}°",
                    5f,
                    y + 8f,
                    labelPaint,
                )
            }

            // Y-axis
            drawLine(
                color = AxisColor,
                start = Offset(leftPadding, topPadding),
                end = Offset(leftPadding, topPadding + graphHeight),
                strokeWidth = 3f,
            )

            // X-axis
            drawLine(
                color = AxisColor,
                start = Offset(leftPadding, topPadding + graphHeight),
                end = Offset(leftPadding + graphWidth, topPadding + graphHeight),
                strokeWidth = 3f,
            )

            // X-axis values
            points.forEachIndexed { index, point ->
                drawContext.canvas.nativeCanvas.drawText(
                    "${index + 1}",
                    point.x - 8f,
                    topPadding + graphHeight + 38f,
                    labelPaint,
                )
            }

            // Area under graph
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.05f),
                    ),
                ),
            )

            // Smooth graph line
            drawPath(
                path = smoothPath,
                color = LineColor,
                style = Stroke(
                    width = 5f,
                    cap = StrokeCap.Round,
                ),
            )

            // Points and visible values
            points.forEachIndexed { index, point ->
                drawCircle(
                    color = PointColor,
                    radius = 7f,
                    center = point,
                )

                drawContext.canvas.nativeCanvas.drawText(
                    "${history[index].formatOneDecimal()}°",
                    point.x - 30f,
                    point.y - 18f,
                    valuePaint,
                )
            }

            // Selected touch value
            selectedIndex?.coerceIn(0, points.lastIndex)?.let { index ->
                val selectedPoint = points[index]
                val selectedValue = history[index]

                drawLine(
                    color = Color.White.copy(alpha = 0.7f),
                    start = Offset(selectedPoint.x, topPadding),
                    end = Offset(selectedPoint.x, topPadding + graphHeight),
                    strokeWidth = 2f,
                )

                drawCircle(
                    color = Color.White,
                    radius = 12f,
                    center = selectedPoint,
                )

                drawCircle(
                    color = PointColor,
                    radius = 7f,
                    center = selectedPoint,
                )

                val tooltipWidth = 130f
                val tooltipHeight = 55f

                val tooltipX = selectedPoint.x - tooltipWidth / 2
                val tooltipY = selectedPoint.y - 85f

                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.75f),
                    topLeft = Offset(tooltipX, tooltipY),
                    size = Size(tooltipWidth, tooltipHeight),
                    cornerRadius = CornerRadius(18f, 18f),
                )

                drawContext.canvas.nativeCanvas.drawText(
                    "${selectedValue.formatOneDecimal()}°C",
                    tooltipX + 22f,
                    tooltipY + 36f,
                    selectedPaint,
                )
            }
        }
    }
}

private fun createSmoothPath(points: List<Offset>): Path {
    return Path().apply {
        moveTo(points.first().x, points.first().y)

        for (i in 0 until points.lastIndex) {
            val current = points[i]
            val next = points[i + 1]

            val controlPoint1 = Offset(
                x = (current.x + next.x) / 2,
                y = current.y,
            )

            val controlPoint2 = Offset(
                x = (current.x + next.x) / 2,
                y = next.y,
            )

            cubicTo(
                controlPoint1.x,
                controlPoint1.y,
                controlPoint2.x,
                controlPoint2.y,
                next.x,
                next.y,
            )
        }
    }
}

private fun findNearestPointIndex(
    tapX: Float,
    historySize: Int,
    canvasWidth: Float,
): Int {
    val leftPadding = 65f
    val rightPadding = 30f
    val graphWidth = canvasWidth - leftPadding - rightPadding

    var nearestIndex = 0
    var smallestDistance = Float.MAX_VALUE

    for (i in 0 until historySize) {
        val pointX = leftPadding + i * graphWidth / (historySize - 1)
        val distance = abs(tapX - pointX)

        if (distance < smallestDistance) {
            smallestDistance = distance
            nearestIndex = i
        }
    }

    return nearestIndex
}

private fun Float.formatOneDecimal(): String {
    return String.format(Locale.getDefault(), "%.1f", this)
}

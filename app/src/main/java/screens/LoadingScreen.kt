package com.example.homeautomation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.homeautomation.ui.theme.Accent
import com.example.homeautomation.ui.theme.TextSoft
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(onFinished: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing), // Faster animation
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        delay(1200) // Reduced from 4000ms to 1200ms
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(modifier = Modifier.size(120.dp)) { // Slightly smaller to be faster to process
                val width = size.width
                val height = size.height
                
                val houseOutline = Path().apply {
                    moveTo(width * 0.2f, height * 0.85f)
                    lineTo(width * 0.8f, height * 0.85f)
                    lineTo(width * 0.8f, height * 0.5f)
                    lineTo(width * 0.5f, height * 0.15f)
                    lineTo(width * 0.2f, height * 0.5f)
                    lineTo(width * 0.2f, height * 0.85f)
                }

                val doorPath = Path().apply {
                    moveTo(width * 0.45f, height * 0.85f)
                    lineTo(width * 0.45f, height * 0.65f)
                    lineTo(width * 0.55f, height * 0.65f)
                    lineTo(width * 0.55f, height * 0.85f)
                }

                val pathMeasure = PathMeasure()
                
                pathMeasure.setPath(houseOutline, false)
                val outlineDrawPath = Path()
                pathMeasure.getSegment(0f, pathMeasure.length * progress, outlineDrawPath, true)

                drawPath(
                    path = outlineDrawPath,
                    color = Accent,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                pathMeasure.setPath(doorPath, false)
                val doorDrawPath = Path()
                val doorProgress = (progress * 1.5f - 0.5f).coerceIn(0f, 1f)
                pathMeasure.getSegment(0f, pathMeasure.length * doorProgress, doorDrawPath, true)
                
                drawPath(
                    path = doorDrawPath,
                    color = Accent,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            Text(
                text = "SMART HOME",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
        }
    }
}

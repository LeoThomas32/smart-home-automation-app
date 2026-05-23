package com.example.homeautomation.uinew.graph

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ScreenGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF070B65),
        Color(0xFF101A3A),
        Color(0xFF050816),
    ),
)

private val GraphCardColor = Color(0x221E88E5)
private val AxisColor = Color.White.copy(alpha = 0.8f)
private val GridColor = Color.White.copy(alpha = 0.18f)
private val LineColor = Color.White
private val PointColor = Color(0xFF2ECC71)

@Composable
fun TemperatureHistoryScreen(
    history: List<Float>,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenGradient)
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = "Temperature History",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(20.dp))

            SmoothTemperatureGraph(
                history = history,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
            )

            Spacer(modifier = Modifier.height(25.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(text = "Back")
            }
        }
    }
}

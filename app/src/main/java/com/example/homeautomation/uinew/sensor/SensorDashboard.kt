package com.example.homeautomation.uinew.sensor

import androidx.compose.foundation.background
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.homeautomation.model.SensorUiState

private val DashboardBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF175B65),
        Color(0xFF101B62),
        Color(0xFF170A64),
    ),
)

private val TemperatureCardGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF4A90E2),
        Color(0xFFE84D8A),
    ),
)

private val FanOnColor = Color(0xFF2ECC71)
private val FanOffColor = Color(0xFFE74C3C)

@Composable
fun SensorDashboard(
    uiState: SensorUiState,
    onFanToggle: (Boolean) -> Unit,
    onTemperatureClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardBackgroundGradient),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 24.dp,
                    top = 80.dp,
                    end = 24.dp,
                    bottom = 24.dp,
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            TemperatureCard(
                temperature = uiState.temperature,
                onClick = onTemperatureClick,
            )

            Spacer(modifier = Modifier.height(28.dp))

            FanControlRow(
                isFanOn = uiState.fanOn,
                onFanToggle = onFanToggle,
            )
        }
    }
}

@Composable
private fun TemperatureCard(
    temperature: Float,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(
                brush = TemperatureCardGradient,
                shape = RoundedCornerShape(22.dp),
            )
            .padding(18.dp),
    ) {
        Column {
            Text(
                text = "Temperature",
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "${temperature.formatOneDecimal()} °C",
                fontSize = 42.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun FanControlRow(
    isFanOn: Boolean,
    onFanToggle: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Fan",
            fontSize = 22.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.width(24.dp))

        Switch(
            checked = isFanOn,
            onCheckedChange = onFanToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = FanOnColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = FanOffColor,
            ),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = if (isFanOn) "ON" else "OFF",
            fontSize = 16.sp,
            color = if (isFanOn) FanOnColor else FanOffColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun Float.formatOneDecimal(): String {
    return String.format(Locale.getDefault(), "%.1f", this)
}

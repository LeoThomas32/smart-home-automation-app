package com.example.homeautomation.model

data class SensorUiState(
    val temperature: Float = 28.5f,
    val fanOn: Boolean = false,
    val temperatureHistory: List<Float> = listOf(
        28.5f,
        29.0f,
        30.2f,
        29.7f,
        31.4f,
        30.8f,
    ),
)

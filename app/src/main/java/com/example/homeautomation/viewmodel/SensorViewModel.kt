package com.example.homeautomation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.homeautomation.model.SensorUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SensorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SensorUiState())
    val uiState: StateFlow<SensorUiState> = _uiState.asStateFlow()

    fun setFanState(isOn: Boolean) {
        _uiState.value = _uiState.value.copy(
            fanOn = isOn,
        )
    }

    fun updateTemperature(value: Float) {
        val updatedHistory = (_uiState.value.temperatureHistory + value)
            .takeLast(10)

        _uiState.value = _uiState.value.copy(
            temperature = value,
            temperatureHistory = updatedHistory,
        )
    }
}

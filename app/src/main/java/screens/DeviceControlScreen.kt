package com.example.homeautomation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.homeautomation.model.Device
import com.example.homeautomation.model.Room
import com.example.homeautomation.ui.theme.Accent
import com.example.homeautomation.ui.theme.Accent2
import com.example.homeautomation.ui.theme.Surface
import com.example.homeautomation.ui.theme.TextSoft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceControlScreen(
    room: Room,
    deviceStatuses: Map<String, String>,
    topicPrefix: String,
    onStateChanged: (Device, Boolean) -> Unit,
    onBack: () -> Unit,
    onAddDevice: (String, String) -> Unit,
    onDeleteDevice: (Device) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var deviceToDelete by remember { mutableStateOf<Device?>(null) }
    var newDeviceName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("light") }
    val deviceTypes = listOf("light", "fan", "ac", "tv", "heater", "fridge", "induction_cooker")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        room.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onBack,
                containerColor = Accent,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, 
                    contentDescription = "Back", 
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        if (room.devices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No devices in this room.", color = TextSoft)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(room.devices) { device ->
                    val dType = device.type.lowercase().trim()
                    val dId = device.id.lowercase().trim()
                    val topic = "${topicPrefix}${dType}/${dId}/control"
                    val status = deviceStatuses[topic] ?: device.status
                    
                    val isOn = when (status.lowercase().trim()) {
                        "true", "1", "on" -> true
                        else -> false
                    }

                    DeviceCard(
                        device = device,
                        isOn = isOn,
                        onToggle = { isChecked ->
                            onStateChanged(device, isChecked)
                        },
                        onDelete = { deviceToDelete = device }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        var expanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Device", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newDeviceName,
                        onValueChange = { newDeviceName = it },
                        label = { Text("Device Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box {
                        OutlinedTextField(
                            value = selectedType.capitalize(),
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Device Type") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Surface)
                        ) {
                            deviceTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.capitalize(), color = Color.White) },
                                    onClick = {
                                        selectedType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newDeviceName.isNotEmpty()) {
                        onAddDevice(newDeviceName, selectedType)
                        newDeviceName = ""
                        showAddDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = Surface
        )
    }

    if (deviceToDelete != null) {
        AlertDialog(
            onDismissRequest = { deviceToDelete = null },
            title = { Text("Delete Device", color = Color.White) },
            text = { Text("Are you sure you want to delete ${deviceToDelete!!.name}?", color = Color.White) },
            confirmButton = {
                Button(onClick = {
                    onDeleteDevice(deviceToDelete!!)
                    deviceToDelete = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deviceToDelete = null }) {
                    Text("Cancel")
                }
            },
            containerColor = Surface
        )
    }
}

@Composable
fun DeviceCard(
    device: Device,
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = Surface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.aspectRatio(0.85f)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(device.icon, fontSize = 24.sp)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                }
            }

            Column {
                Text(
                    text = device.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isOn) "On" else "Off",
                        color = TextSoft,
                        fontSize = 14.sp
                    )
                    Switch(
                        checked = isOn,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Accent,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Accent2,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

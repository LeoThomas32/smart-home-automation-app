package com.example.homeautomation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.homeautomation.model.House
import com.example.homeautomation.model.User
import com.example.homeautomation.ui.theme.Accent
import com.example.homeautomation.ui.theme.Surface
import com.example.homeautomation.ui.theme.TextSoft

@Composable
fun HouseSelectionScreen(
    user: User,
    onHouseSelected: (House) -> Unit,
    onAddHouse: (String, String, String, String) -> Unit,
    onDeleteHouse: (House) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newHouseName by remember { mutableStateOf("") }
    var newCountry by remember { mutableStateOf("") }
    var newState by remember { mutableStateOf("") }
    var newCity by remember { mutableStateOf("") }
    var houseToDelete by remember { mutableStateOf<House?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Accent,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add House")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Hello ${user.name}",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Select a house to control",
                color = TextSoft,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(user.houses) { house ->
                    HouseCard(
                        house = house, 
                        onClick = { onHouseSelected(house) },
                        onDelete = { houseToDelete = house }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New House", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newHouseName,
                        onValueChange = { newHouseName = it },
                        label = { Text("House Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCountry,
                        onValueChange = { newCountry = it },
                        label = { Text("Country") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newState,
                        onValueChange = { newState = it },
                        label = { Text("State") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCity,
                        onValueChange = { newCity = it },
                        label = { Text("City") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newHouseName.isNotEmpty()) {
                        onAddHouse(newHouseName, newCountry, newState, newCity)
                        newHouseName = ""
                        newCountry = ""
                        newState = ""
                        newCity = ""
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

    if (houseToDelete != null) {
        AlertDialog(
            onDismissRequest = { houseToDelete = null },
            title = { Text("Delete House", color = Color.White) },
            text = { Text("Are you sure you want to delete ${houseToDelete!!.name}?", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteHouse(houseToDelete!!)
                        houseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { houseToDelete = null }) {
                    Text("Cancel")
                }
            },
            containerColor = Surface
        )
    }
}

@Composable
fun HouseCard(house: House, onClick: () -> Unit, onDelete: () -> Unit) {
    Surface(
        color = Surface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏠", fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = house.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${house.floors.sumOf { it.rooms.size }} Rooms",
                        color = TextSoft,
                        fontSize = 14.sp
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
            }
        }
    }
}

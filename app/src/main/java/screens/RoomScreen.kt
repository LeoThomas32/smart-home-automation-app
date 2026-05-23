package com.example.homeautomation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.homeautomation.model.Floor
import com.example.homeautomation.model.House
import com.example.homeautomation.model.Room
import com.example.homeautomation.model.User
import com.example.homeautomation.network.RetrofitClient
import com.example.homeautomation.ui.theme.Accent
import com.example.homeautomation.ui.theme.Surface
import com.example.homeautomation.ui.theme.TextSoft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun RoomScreen(
    user: User,
    house: House,
    onRoomSelected: (Floor, Room) -> Unit,
    onMenuClick: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    onAddFloor: (String) -> Unit,
    onAddRoom: (Floor, String) -> Unit,
    onDeleteFloor: (Floor) -> Unit,
    onDeleteRoom: (Floor, Room) -> Unit
) {
    var showLogoutMenu by remember { mutableStateOf(false) }
    var showAddFloorDialog by remember { mutableStateOf(false) }
    var showAddRoomDialog by remember { mutableStateOf<Floor?>(null) }
    var newName by remember { mutableStateOf("") }
    
    var floorToDelete by remember { mutableStateOf<Floor?>(null) }
    var roomToDelete by remember { mutableStateOf<Pair<Floor, Room>?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Box {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .clickable { showLogoutMenu = true }
                    ) {
                        Text(
                            text = if (user.name.isNotEmpty()) user.name.take(1).uppercase() else "U",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showLogoutMenu,
                        onDismissRequest = { showLogoutMenu = false },
                        modifier = Modifier.background(Surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Logout", color = Color.White) },
                            onClick = {
                                showLogoutMenu = false
                                onLogout()
                            },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Accent)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Greeting
            Text(
                text = "Hello ${user.name} 👋",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Welcome to ${house.name}",
                    color = TextSoft,
                    fontSize = 16.sp
                )
                TextButton(onClick = { showAddFloorDialog = true }) {
                    Text("+ Add Floor", color = Accent)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weather Card (Now Dynamic)
            WeatherCard(house.city, house.country)

            Spacer(modifier = Modifier.height(32.dp))

            // Content
            if (house.floors.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No floors added. Use '+ Add Floor' to start.", color = TextSoft)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    house.floors.forEach { floor ->
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = floor.name,
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { floorToDelete = floor }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Floor", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                                    }
                                }
                                TextButton(onClick = { showAddRoomDialog = floor }) {
                                    Text("+ Add Room", color = Accent, fontSize = 14.sp)
                                }
                            }
                        }
                        
                        items(floor.rooms) { room ->
                            RoomCard(
                                room = room, 
                                floorName = null, 
                                onClick = { onRoomSelected(floor, room) },
                                onDelete = { roomToDelete = Pair(floor, room) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddFloorDialog) {
        AlertDialog(
            onDismissRequest = { showAddFloorDialog = false },
            title = { Text("Add New Floor", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Floor Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotEmpty()) {
                        onAddFloor(newName)
                        newName = ""
                        showAddFloorDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFloorDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = Surface
        )
    }

    if (showAddRoomDialog != null) {
        AlertDialog(
            onDismissRequest = { showAddRoomDialog = null },
            title = { Text("Add Room to ${showAddRoomDialog!!.name}", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Room Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotEmpty()) {
                        onAddRoom(showAddRoomDialog!!, newName)
                        newName = ""
                        showAddRoomDialog = null
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRoomDialog = null }) {
                    Text("Cancel")
                }
            },
            containerColor = Surface
        )
    }

    if (floorToDelete != null) {
        AlertDialog(
            onDismissRequest = { floorToDelete = null },
            title = { Text("Delete Floor", color = Color.White) },
            text = { Text("Are you sure you want to delete ${floorToDelete!!.name} and all its rooms?", color = Color.White) },
            confirmButton = {
                Button(onClick = {
                    onDeleteFloor(floorToDelete!!)
                    floorToDelete = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { floorToDelete = null }) {
                    Text("Cancel")
                }
            },
            containerColor = Surface
        )
    }

    if (roomToDelete != null) {
        AlertDialog(
            onDismissRequest = { roomToDelete = null },
            title = { Text("Delete Room", color = Color.White) },
            text = { Text("Are you sure you want to delete ${roomToDelete!!.second.name}?", color = Color.White) },
            confirmButton = {
                Button(onClick = {
                    onDeleteRoom(roomToDelete!!.first, roomToDelete!!.second)
                    roomToDelete = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { roomToDelete = null }) {
                    Text("Cancel")
                }
            },
            containerColor = Surface
        )
    }
}

@Composable
fun WeatherCard(city: String, country: String) {
    var temp by remember { mutableStateOf("--") }
    var description by remember { mutableStateOf("Loading...") }
    var icon by remember { mutableStateOf("⛅") }

    LaunchedEffect(city) {
        if (city.isEmpty()) return@LaunchedEffect
        
        try {
            // 1. Geocode City to Lat/Long (Open-Meteo Geocoding is free/no key)
            val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$city&count=1&language=en&format=json"
            val geoResp = withContext(Dispatchers.IO) { RetrofitClient.instance.getExternalData(geoUrl) }
            
            if (geoResp.isSuccessful) {
                val results = geoResp.body()?.get("results") as? List<Map<String, Any>>
                if (!results.isNullOrEmpty()) {
                    val lat = results[0]["latitude"]
                    val lon = results[0]["longitude"]
                    
                    // 2. Fetch Weather (Open-Meteo is free/no key)
                    val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true"
                    val weatherResp = withContext(Dispatchers.IO) { RetrofitClient.instance.getExternalData(weatherUrl) }
                    
                    if (weatherResp.isSuccessful) {
                        val current = weatherResp.body()?.get("current_weather") as? Map<String, Any>
                        if (current != null) {
                            temp = "${current["temperature"]?.toString()?.split(".")?.get(0)}°"
                            val code = (current["weathercode"] as? Double)?.toInt() ?: 0
                            
                            // Map WMO codes to text/icons
                            val (desc, ico) = when(code) {
                                0 -> "Clear sky" to "☀️"
                                1, 2, 3 -> "Partly cloudy" to "⛅"
                                45, 48 -> "Foggy" to "🌫️"
                                51, 53, 55 -> "Drizzle" to "🌧️"
                                61, 63, 65 -> "Rainy" to "🌧️"
                                71, 73, 75 -> "Snowy" to "❄️"
                                80, 81, 82 -> "Showers" to "🌦️"
                                95, 96, 99 -> "Stormy" to "⛈️"
                                else -> "Cloudy" to "☁️"
                            }
                            description = desc
                            icon = ico
                        }
                    }
                }
            }
        } catch (e: Exception) {
            description = "Weather offline"
        }
    }

    Surface(
        color = Surface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(icon, fontSize = 40.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = description, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text(text = if (city.isNotEmpty()) "$city, $country" else "Add a city in House settings", color = TextSoft, fontSize = 14.sp)
                    }
                }
                Text(text = temp, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Light)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherStat("27°C", "Sensible")
                WeatherStat("4%", "Precipitation")
                WeatherStat("66%", "Humidity")
                WeatherStat("16 km/h", "Wind")
            }
        }
    }
}

@Composable
fun WeatherStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(text = label, color = TextSoft, fontSize = 10.sp)
    }
}

@Composable
fun RoomCard(room: Room, floorName: String?, onClick: () -> Unit, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(0.9f)
            .clip(RoundedCornerShape(24.dp))
            .background(Surface)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Room", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            }
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(room.icon, fontSize = 32.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = room.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (floorName != null) {
                Text(text = floorName, color = TextSoft, fontSize = 10.sp)
            }
            Text(text = "${room.devices.size} Devices", color = TextSoft, fontSize = 12.sp)
        }
    }
}

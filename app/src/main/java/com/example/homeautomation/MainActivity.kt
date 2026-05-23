package com.example.homeautomation

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.*
import com.example.homeautomation.model.House
import com.example.homeautomation.model.Room
import com.example.homeautomation.model.User
import com.example.homeautomation.model.Floor
import com.example.homeautomation.mqtt.CommandPublisher
import com.example.homeautomation.mqtt.MqttHelper
import com.example.homeautomation.network.RetrofitClient
import com.example.homeautomation.screens.*
import com.example.homeautomation.ui.theme.HomeAutomationTheme
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var mqtt: MqttHelper
    private val deviceStatuses = mutableStateMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)

        mqtt = MqttHelper(this)
        mqtt.setCallback { topic, payload ->
            runOnUiThread {
                // Lowercase topic key for consistent lookup in DeviceControlScreen
                deviceStatuses[topic.lowercase().trim()] = payload
            }
        }
        mqtt.connect()

        val publisher = CommandPublisher(mqtt)

        setContent {
            HomeAutomationTheme {
                var currentScreen by remember { mutableStateOf("loading") }
                var isFetching by remember { mutableStateOf(false) }
                var currentUser by remember { mutableStateOf<User?>(null) }
                var userHouses by remember { mutableStateOf<List<House>>(emptyList()) }
                var selectedHouse by remember { mutableStateOf<House?>(null) }
                var selectedFloor by remember { mutableStateOf<Floor?>(null) }
                var selectedRoom by remember { mutableStateOf<Room?>(null) }

                val scope = rememberCoroutineScope()

                fun fetchConfig(userId: String) {
                    isFetching = true
                    scope.launch {
                        try {
                            val resp = RetrofitClient.instance.getConfig(userId)
                            if (resp.isSuccessful) {
                                userHouses = resp.body() ?: emptyList()
                                currentUser = User(userId, userId.replaceFirstChar { it.uppercase() }, userHouses)
                                
                                if (selectedHouse != null) {
                                    selectedHouse = userHouses.find { it.id == selectedHouse!!.id }
                                }
                                if (selectedFloor != null && selectedHouse != null) {
                                    selectedFloor = selectedHouse!!.floors.find { it.id == selectedFloor!!.id }
                                }
                                if (selectedRoom != null && selectedFloor != null) {
                                    selectedRoom = selectedFloor!!.rooms.find { it.id == selectedRoom!!.id }
                                }
                                
                                if (currentScreen == "loading" || currentScreen == "login") {
                                    currentScreen = "house_selection"
                                }
                            } else {
                                if (currentScreen == "loading") currentScreen = "login"
                            }
                        } catch (e: Exception) {
                            if (currentScreen == "loading") currentScreen = "login"
                        } finally {
                            isFetching = false
                        }
                    }
                }

                fun deleteItem(params: Map<String, String>) {
                    scope.launch {
                        try {
                            val resp = RetrofitClient.instance.deleteElement(params)
                            if (resp.isSuccessful) {
                                fetchConfig(currentUser!!.id)
                                Toast.makeText(this@MainActivity, "Deleted successfully", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "Delete failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                if (isFetching && currentScreen != "loading") {
                    LoadingScreen(onFinished = {})
                } else {
                    when (currentScreen) {
                        "loading" -> LoadingScreen(
                            onFinished = {
                                val savedUserId = sharedPref.getString("user_id", null)?.lowercase()
                                if (savedUserId != null) {
                                    fetchConfig(savedUserId)
                                } else {
                                    currentScreen = "login"
                                }
                            }
                        )

                        "login" -> LoginScreen(
                            onLoginSuccess = { user ->
                                sharedPref.edit().putString("user_id", user.id).apply()
                                fetchConfig(user.id)
                            }
                        )

                        "house_selection" -> currentUser?.let { user ->
                            HouseSelectionScreen(
                                user = user.copy(houses = userHouses),
                                onHouseSelected = { house ->
                                    selectedHouse = house
                                    // Subscribe to specific house for real-time updates
                                    mqtt.subscribe("users/${user.id.lowercase()}/houses/${house.id.lowercase()}/#")
                                    currentScreen = "room"
                                },
                                onAddHouse = { name, country, state, city ->
                                    val uniqueId = name.lowercase().replace(" ", "_") + "_" + UUID.randomUUID().toString().take(4)
                                    scope.launch {
                                        RetrofitClient.instance.addElement(mapOf(
                                            "username" to user.id,
                                            "house_id" to uniqueId,
                                            "house_name" to name,
                                            "country" to country,
                                            "state" to state,
                                            "city" to city
                                        ))
                                        fetchConfig(user.id)
                                    }
                                },
                                onDeleteHouse = { house ->
                                    deleteItem(mapOf("username" to user.id, "house_id" to house.id))
                                }
                            )
                        }

                        "room" -> selectedHouse?.let { house ->
                            RoomScreen(
                                user = currentUser!!,
                                house = house,
                                onRoomSelected = { floor, room ->
                                    selectedFloor = floor
                                    selectedRoom = room
                                    currentScreen = "device"
                                },
                                onMenuClick = { currentScreen = "house_selection" },
                                onLogout = {
                                    sharedPref.edit().clear().apply()
                                    currentUser = null
                                    currentScreen = "login"
                                },
                                onBack = { currentScreen = "house_selection" },
                                onAddFloor = { name ->
                                    val uniqueId = name.lowercase().replace(" ", "_") + "_" + UUID.randomUUID().toString().take(4)
                                    scope.launch {
                                        RetrofitClient.instance.addElement(mapOf(
                                            "username" to currentUser!!.id,
                                            "house_id" to house.id,
                                            "house_name" to house.name,
                                            "floor_id" to uniqueId,
                                            "floor_name" to name
                                        ))
                                        fetchConfig(currentUser!!.id)
                                    }
                                },
                                onAddRoom = { floor, name ->
                                    val uniqueId = name.lowercase().replace(" ", "_") + "_" + UUID.randomUUID().toString().take(4)
                                    scope.launch {
                                        RetrofitClient.instance.addElement(mapOf(
                                            "username" to currentUser!!.id,
                                            "house_id" to house.id,
                                            "house_name" to house.name,
                                            "floor_id" to floor.id,
                                            "floor_name" to floor.name,
                                            "room_id" to uniqueId,
                                            "room_name" to name
                                        ))
                                        fetchConfig(currentUser!!.id)
                                    }
                                },
                                onDeleteFloor = { floor ->
                                    deleteItem(mapOf("username" to currentUser!!.id, "house_id" to house.id, "floor_id" to floor.id))
                                },
                                onDeleteRoom = { floor, room ->
                                    deleteItem(mapOf("username" to currentUser!!.id, "house_id" to house.id, "floor_id" to floor.id, "room_id" to room.id))
                                }
                            )
                        }

                        "device" -> selectedRoom?.let { room ->
                            val userId = (currentUser?.id ?: "user1").lowercase()
                            val houseId = (selectedHouse?.id ?: "house1").lowercase()
                            val floorId = (selectedFloor?.id ?: "floor1").lowercase()
                            val roomId = room.id.lowercase()
                            // Standardized prefix without trailing slash to control segments manually
                            val topicPrefix = "users/$userId/houses/$houseId/floors/$floorId/rooms/$roomId/devices"

                            DeviceControlScreen(
                                room = room,
                                deviceStatuses = deviceStatuses,
                                topicPrefix = "$topicPrefix/",
                                onStateChanged = { device, isOn ->
                                    val dType = device.type.lowercase()
                                    val dId = device.id.lowercase()
                                    val topic = "$topicPrefix/$dType/$dId/control"
                                    val payload = if (isOn) "ON" else "OFF"
                                    deviceStatuses[topic] = payload
                                    publisher.send(topic, isOn)
                                    
                                    scope.launch {
                                        RetrofitClient.instance.updateStatus(mapOf(
                                            "username" to userId,
                                            "house_id" to houseId,
                                            "floor_id" to floorId,
                                            "room_id" to roomId,
                                            "device_id" to dId,
                                            "status" to payload
                                        ))
                                    }
                                },
                                onBack = { currentScreen = "room" },
                                onAddDevice = { name, type ->
                                    val uniqueId = name.lowercase().replace(" ", "_") + "_" + UUID.randomUUID().toString().take(4)
                                    scope.launch {
                                        RetrofitClient.instance.addElement(mapOf(
                                            "username" to userId,
                                            "house_id" to houseId,
                                            "house_name" to selectedHouse!!.name,
                                            "floor_id" to floorId,
                                            "floor_name" to selectedFloor!!.name,
                                            "room_id" to roomId,
                                            "room_name" to room.name,
                                            "device_id" to uniqueId,
                                            "device_name" to name,
                                            "device_type" to type
                                        ))
                                        fetchConfig(userId)
                                    }
                                },
                                onDeleteDevice = { device ->
                                    deleteItem(mapOf(
                                        "username" to userId,
                                        "house_id" to houseId,
                                        "floor_id" to floorId,
                                        "room_id" to room.id,
                                        "device_id" to device.id
                                    ))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

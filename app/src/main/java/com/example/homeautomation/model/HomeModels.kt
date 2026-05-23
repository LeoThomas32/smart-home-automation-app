package com.example.homeautomation.model

data class Device(
    val id: String,
    val name: String,
    val icon: String,
    val type: String,
    val status: String = "OFF"
)

data class Room(
    val id: String,
    val name: String,
    val icon: String,
    val devices: List<Device>
)

data class Floor(
    val id: String,
    val name: String,
    val rooms: List<Room>
)

data class House(
    val id: String,
    val name: String,
    val country: String = "",
    val state: String = "",
    val city: String = "",
    val floors: List<Floor>
)

data class User(
    val id: String,
    val name: String,
    val houses: List<House>
)

val usersData = listOf(
    User(
        id = "user1",
        name = "Messi",
        houses = listOf(
            House(
                id = "house1",
                name = "Main Home",
                floors = listOf(
                    Floor(
                        id = "floor1",
                        name = "Ground Floor",
                        rooms = listOf(
                            Room("master_bedroom", "Master Bedroom", "🛏️", listOf(
                                Device("fan", "Fan", "🌬️", "fan"),
                                Device("light", "Light", "💡", "light"),
                                Device("ac", "AC", "❄️", "ac")
                            )),
                            Room("living_room", "Living Room", "🛋️", listOf(
                                Device("fan", "Fan", "🌬️", "fan"),
                                Device("light", "Light", "💡", "light"),
                                Device("tv", "TV", "📺", "tv")
                            )),
                            Room("kitchen", "Kitchen", "🍳", listOf(
                                Device("light", "Light", "💡", "light"),
                                Device("fridge", "Fridge", "🧊", "fridge"),
                                Device("induction_cooker", "Induction Cooker", "🍳", "induction_cooker")
                            )),
                            Room("bathroom", "Bathroom", "🚿", listOf(
                                Device("light", "Light", "💡", "light"),
                                Device("heater", "Heater", "🔥", "heater")
                            ))
                        )
                    )
                )
            )
        )
    ),
    User(
        id = "user2",
        name = "Ronaldo",
        houses = listOf(
            House(
                id = "house1",
                name = "User2 House 1",
                floors = listOf(
                    Floor(
                        id = "floor1",
                        name = "Floor 1",
                        rooms = listOf(
                            Room("master_bedroom", "Master Bedroom", "🛏️", listOf(
                                Device("fan", "Fan", "🌬️", "fan"),
                                Device("light", "Light", "💡", "light"),
                                Device("ac", "AC", "❄️", "ac")
                            )),
                            Room("living_room", "Living Room", "🛋️", listOf(
                                Device("fan", "Fan", "🌬️", "fan"),
                                Device("light", "Light", "💡", "light"),
                                Device("tv", "TV", "📺", "tv")
                            )),
                            Room("kitchen", "Kitchen", "🍳", listOf(
                                Device("light", "Light", "💡", "light"),
                                Device("fridge", "Fridge", "🧊", "fridge"),
                                Device("induction_cooker", "Induction Cooker", "🍳", "induction_cooker")
                            )),
                            Room("bathroom", "Bathroom", "🚿", listOf(
                                Device("light", "Light", "💡", "light"),
                                Device("heater", "Heater", "🔥", "heater")
                            ))
                        )
                    )
                )
            ),
            House(
                id = "house2",
                name = "User2 House 2",
                floors = listOf(
                    Floor(
                        id = "floor1",
                        name = "Floor 1",
                        rooms = listOf(
                            Room("master_bedroom", "Master Bedroom", "🛏️", listOf(
                                Device("fan", "Fan", "🌬️", "fan"),
                                Device("light", "Light", "💡", "light"),
                                Device("ac", "AC", "❄️", "ac")
                            )),
                            Room("living_room", "Living Room", "🛋️", listOf(
                                Device("fan", "Fan", "🌬️", "fan"),
                                Device("light", "Light", "💡", "light"),
                                Device("tv", "TV", "📺", "tv")
                            )),
                            Room("kitchen", "Kitchen", "🍳", listOf(
                                Device("light", "Light", "💡", "light"),
                                Device("fridge", "Fridge", "🧊", "fridge"),
                                Device("induction_cooker", "Induction Cooker", "🍳", "induction_cooker")
                            )),
                            Room("bathroom", "Bathroom", "🚿", listOf(
                                Device("light", "Light", "💡", "light"),
                                Device("heater", "Heater", "🔥", "heater")
                            ))
                        )
                    ),
                    Floor(
                        id = "floor2",
                        name = "Floor 2",
                        rooms = listOf(
                            Room("bedroom1", "Bedroom 1", "🛏️", listOf(
                                Device("light", "Light", "💡", "light"),
                                Device("ac", "AC", "❄️", "ac")
                            )),
                            Room("bedroom2", "Bedroom 2", "🛏️", listOf(
                                Device("light", "Light", "💡", "light"),
                                Device("ac", "AC", "❄️", "ac")
                            )),
                            Room("bathroom", "Bathroom", "🚿", listOf(
                                Device("light", "Light", "💡", "light"),
                                Device("heater", "Heater", "🔥", "heater")
                            ))
                        )
                    )
                )
            )
        )
    )
)

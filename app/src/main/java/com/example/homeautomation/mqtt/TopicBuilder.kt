package com.example.homeautomation.mqtt

object TopicBuilder {

    fun controlTopic(

        user: String,
        house: String,
        floor: String,
        room: String,
        device: String,
        id: String

    ): String {

        return "users/$user/" +
                "houses/$house/" +
                "floors/$floor/" +
                "rooms/$room/" +
                "devices/$device/" +
                "$id/control"

    }

}
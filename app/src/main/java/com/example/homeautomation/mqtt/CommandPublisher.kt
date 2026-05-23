package com.example.homeautomation.mqtt

class CommandPublisher(

    private val mqtt:
    MqttHelper

) {

    fun send(

        topic: String,

        enabled: Boolean

    ) {

        mqtt.publish(

            topic,

            if (

                enabled

            )

                "ON"

            else

                "OFF"

        )

    }

}
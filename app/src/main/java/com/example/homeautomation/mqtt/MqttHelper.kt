package com.example.homeautomation.mqtt

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.UUID

class MqttHelper(
    private val context: Context
) {
    // Use WebSockets port 8000 as it's most compatible with mobile networks
    private val broker = "ws://broker.hivemq.com:8000/mqtt"
    private var client: MqttClient? = null
    private val subscriptions = mutableSetOf<String>()
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private var onMessageReceived: ((String, String) -> Unit)? = null

    fun setCallback(callback: (String, String) -> Unit) {
        onMessageReceived = callback
    }

    fun connect() {
        ioScope.launch {
            try {
                if (client == null) {
                    val clientId = "HomeAuto_" + UUID.randomUUID().toString().take(8)
                    // MemoryPersistence is CRITICAL for Android to avoid permission issues
                    client = MqttClient(
                        broker,
                        clientId,
                        MemoryPersistence()
                    )

                    client?.setCallback(object : MqttCallbackExtended {
                        override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                            Log.d("MQTT", "Connection Successful! $serverURI")
                            subscriptions.forEach { topic ->
                                subscribeInternal(topic)
                            }
                        }

                        override fun connectionLost(cause: Throwable?) {
                            Log.e("MQTT", "Connection Lost: ${cause?.message}")
                        }

                        override fun messageArrived(topic: String?, message: MqttMessage?) {
                            topic?.let { t ->
                                message?.let { m ->
                                    val payload = String(m.payload).trim()
                                    Log.d("MQTT", "Received: $t -> $payload")
                                    onMessageReceived?.invoke(t, payload)
                                }
                            }
                        }

                        override fun deliveryComplete(token: IMqttDeliveryToken?) {}
                    })
                }

                if (client?.isConnected != true) {
                    val options = MqttConnectOptions()
                    options.isCleanSession = true 
                    options.isAutomaticReconnect = true
                    options.connectionTimeout = 30 // Increased for mobile latency
                    options.keepAliveInterval = 60 
                    
                    Log.d("MQTT", "Attempting to connect to $broker...")
                    client?.connect(options)
                }
            } catch (e: Exception) {
                Log.e("MQTT", "Connection Failed: ${e.message}", e)
            }
        }
    }

    fun subscribe(topic: String) {
        subscriptions.add(topic)
        if (client?.isConnected == true) {
            ioScope.launch {
                subscribeInternal(topic)
            }
        }
    }

    private fun subscribeInternal(topic: String) {
        try {
            client?.subscribe(topic, 1)
            Log.d("MQTT", "Subscribed to: $topic")
        } catch (e: Exception) {
            Log.e("MQTT", "Subscribe failed for $topic", e)
        }
    }

    fun publish(topic: String, payload: String) {
        ioScope.launch {
            try {
                if (client?.isConnected != true) {
                    Log.d("MQTT", "Not connected, attempting to reconnect...")
                    connect()
                    // Wait briefly for connection
                    var attempts = 0
                    while (client?.isConnected != true && attempts < 10) {
                        kotlinx.coroutines.delay(500)
                        attempts++
                    }
                }

                if (client?.isConnected == true) {
                    val message = MqttMessage(payload.toByteArray())
                    message.qos = 1
                    message.isRetained = true 
                    client?.publish(topic, message)
                    Log.d("MQTT", "Published: $topic -> $payload")
                } else {
                    Log.e("MQTT", "Publish failed: MQTT not connected")
                }
            } catch (e: Exception) {
                Log.e("MQTT", "Publish failed", e)
            }
        }
    }
}

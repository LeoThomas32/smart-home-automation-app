import paho.mqtt.client as mqtt
import random
import time

broker = "broker.hivemq.com"
port = 1883
topic = "home/sensor/humidity"

#Connect to broker
client = mqtt.Client()
client.connect(broker,port,60)
client.loop_start()

print("Connected to MQTT broker")
print("Publishing humidity data every 5 seconds...")

while True:
    #Generate random values
    humidity = random.randint(40,90)
    
    #Publish value to MQTT broker
    client.publish(topic,str(humidity))
    print(f"Published humidity: {humidity}%")

    #wait
    time.sleep(5)
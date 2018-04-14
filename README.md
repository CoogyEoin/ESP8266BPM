# ESP8266BPM
As part of my final year project I created a heart rate monitor on the ESP8266 using the Arduino IDE. The program measures the period of the pulse and uses it to calculate the BPM. 

Firebase was used to store user's data and register users on the android app. Firebase cloud messaging doesn't work with the ESP8266 as of writing this so the legacy code for Google cloud messaging was used to send push notifications to the user's phone.

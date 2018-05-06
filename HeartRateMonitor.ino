#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>\Z
#include <ESP8266HTTPClient.h>
#include <FirebaseCloudMessaging.h>
 
#define FIREBASE_HOST "heartdetect-581b9.firebaseio.com"
#define FIREBASE_AUTH ""
#define WIFI_SSID "AndroidAP"
#define WIFI_PASSWORD ""
#define CLIENT_REGISTRATION_ID ""
#define SERVER_KEY ""
#define userToken ""

const int numReadings = 10;
long times[3] = {0,0,0};
int readings[numReadings]; 
int readIndex = 0;             
int total = 0;
int counter = 0;                            
int previousData = 0;
int newData = 0;
bool firstDetection = false;
bool secondDetection = false;
bool ignore = false;
bool sendMessageOne = false;
bool sendMessageTwo = false;

HTTPClient http;
void sendPushNotification(String title, String message) {

//String in JSON format//
  String data = "{";
  data = data + "\"to\": \""CLIENT_REGISTRATION_ID"\",";
  data = data + "\"notification\": {";
  data = data + "\"body\": \"" + message + "\",";
  data = data + "\"title\" : \"" + title + "\" ";
  data = data + "} }";

  http.begin("http://fcm.googleapis.com/fcm/send");
  
  //Sets up the extra headers//
  http.addHeader("Authorization", "key="SERVER_KEY);
  http.addHeader("Content-Type", "application/json");
  http.addHeader("Host", "fcm.googleapis.com");
  http.addHeader("Content-Length", String(message.length()));

  //POST request of the String with the relevant message//
  http.POST(data);
  http.writeToStream(&Serial);
  http.end();

}


void setup() {
  Serial.begin(9600);
  for (int thisReading = 0; thisReading < numReadings; thisReading++) {
    readings[thisReading] = 0;
  }
 
   // connect to wifi.
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("connecting");
  while (WiFi.status() != WL_CONNECTED) {
    //Serial.print(".");
    delay(500);
  }
  //Serial.println();
  //Serial.print("connected: ");
  //Serial.println(WiFi.localIP());
  
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
}
 
void loop() {
  
  previousData = newData; //Set previous value//

  //Moving average Filter of 10 samples//
  total = total - readings[readIndex];
  readings[readIndex] = analogRead(A0);
  total = total + readings[readIndex];
  readIndex = readIndex + 1;

 //Set the samples back to 10//
  if (readIndex >= numReadings) {
    readIndex = 0;
  }
 
  newData = total / numReadings;
 
 //Slope detection while ignoring the values below the lower threshold//
if (previousData < 32 && newData >= 32 && ignore == false) {
  if(firstDetection = false){ 
    times[0] = millis();
    firstDetection = true;
  }else{
    times[1] = millis();
    times[2] = times[1] - times[0];
    times[0] = times[1];
    firstDetection = false;
  }
  ignore = true;
}

//Ignore the values below a certain point to account for second rise in the pulse//
if(newData < 22){
  ignore = false;
}
 
float BPM =  (1.0 /(times[2])) * 60.0 * 1000;
Serial.println(newData);
 
//Communicating with the server takes a few seconds so the BPM is only input every few cycles.//
//The amount of notifications I got on my phone when testing was rediculous so I set it to a few thousand cycles before the push notification sent//
if(counter >= 20){
  
  //Send BPM value to database under the user token//
  Firebase.setFloat(userToken"/Heartrate", BPM);

 /*
  if (Firebase.failed()) {
      Serial.print("setting /message failed:");
      Serial.println(Firebase.error());  
      return;
  }
  */

//Send push notification if BPM is too high//
if(BPM > 100 && sendMessageOne == false){
  
sendPushNotification("Attention! ", "Your BPM is too high");
  /*
  FirebaseCloudMessaging fcm(SERVER_KEY);
  FirebaseCloudMessage message =
    FirebaseCloudMessage::SimpleNotification("Attention!", "Your BPM is too high!");
  FirebaseError error = fcm.SendMessageToUser(CLIENT_REGISTRATION_ID, message);
  if (error) {
    Serial.print("Error:");
    Serial.print(error.code());
    Serial.print(" :: ");
    Serial.println(error.message().c_str());
  } else {
    Serial.println("Sent OK!");
  }
  */
  sendMessageOne = true;
}

if(BPM < 40 && sendMessageTwo == false){
  
sendPushNotification("Attention! ", "Your BPM is too low");
  /*
  FirebaseCloudMessaging fcm(SERVER_KEY);
  FirebaseCloudMessage message =
    FirebaseCloudMessage::SimpleNotification("Attention!", "Your BPM is too high!");
  FirebaseError error = fcm.SendMessageToUser(CLIENT_REGISTRATION_ID, message);
  if (error) {
    Serial.print("Error:");
    Serial.print(error.code());
    Serial.print(" :: ");
    Serial.println(error.message().c_str());
  } else {
    Serial.println("Sent OK!");
  }
  */
  sendMessageTwo = true;
}


//Set counter back to zero after 20 cycles//
counter = 0;
}else{
  //Adds to counter for next cycle//
  counter = counter + 1;
}
 
}
 
 



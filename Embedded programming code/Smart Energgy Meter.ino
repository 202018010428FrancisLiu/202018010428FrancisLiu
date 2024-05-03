#include <Arduino.h>
#include <SoftwareSerial.h>
#define BLINKER_WIFI
#include <Blinker.h>
#include <PubSubClient.h>
#include <WiFi.h>

#define MYPORT_TX 4
#define MYPORT_RX 5
byte SerialTemps[25];   // Serial buffer
byte SeriaDataLen = 0;  // Data length counter
bool SerialRead = 0;    // Serial data OK flag

uint32_t VolPar;       // Voltage parameter
uint32_t CurrentPar;   // Current parameter
uint32_t PowerPar;     // Power parameter
uint32_t CurrentData;  // Current data
float VF;              // Voltage coefficient
float CF;              // Current coefficient

float Active_Volt;                 // Store effective voltage
float Active_Current;              // Store effective current
float Active_Power;                // Store effective power
double totalEnergyConsumed = 0.0;  // Store energy consumed variable using double type

uint32_t PF_One;    // Store the number of pulses required for 1° of electricity
uint32_t PF_All;    // Pulse count from power on
float Used_KWh;     // Store used electricity
uint8_t SysStatus;  // System status register

uint32_t VolData;          // Voltage data
uint32_t PowerData;        // Power data
uint16_t PF;               // Pulse counter
uint32_t PFData = 1;       // Pulse overflow counter
uint32_t VolR1 = 1880000;  // Voltage resistor 1 470K*4  1880K
uint32_t VolR2 = 1000;     // Voltage resistor 2  1K
float CurrentRF = 0.001;   // Current sampling resistor 0.001 ohm
float limitCurrent = 10;   // Default maximum current limit in Amperes

void setVF(float Data);    // Write voltage coefficient
void setCF(float Data);    // Write current coefficient
void SerialReadLoop();     // Serial loop business, get data and decode it
float GetVol();            // Get voltage
float GetVolAnalog();      // Get raw ADC value of voltage
float GetCurrent();        // Get current
float GetCurrentAnalog();  // Get raw ADC value of current
float GetActivePower();    // Get active power
bool Checksum();           // Checksum function

char auth[] = "1f744a1a5884";
char ssid[] = "liuxiang";
char pswd[] = "12345678";

unsigned long lastSendTime = 0;
const unsigned long sendInterval = 10000;

char mqtt_server[] = "mqtts.heclouds.com";
uint32_t mqtt_port = 1883;
char deviceId[] = "light-1";
char pubId[] = "lZXH30KI5f";
char password[] = "version=2018-10-31&res=products%2FlZXH30KI5f%2Fdevices%2Flight-1&et=4081041401&method=md5&sign=NG6Fa8BPYb3CvhB7d9V4AQ%3D%3D";
char dataTopic[] = "$sys/lZXH30KI5f/light-1/thing/property/post";
WiFiClient espClient;
PubSubClient mqttClient(espClient);

BlinkerNumber Number1("num-A");
BlinkerNumber Number2("num-B");
BlinkerNumber Number3("num-C");
BlinkerNumber Number4("num-D");
void heartbeat() {
  Number1.print(Active_Volt);
  Number2.print(Active_Current);
  Number3.print(Active_Power);
  Number4.print(totalEnergyConsumed);
}

EspSoftwareSerial::UART myPort;
void setVF(float Data)  // Write voltage coefficient, input value is voltage, calculate and get corrected value
{
  VF = Data;
}
void setCF(float Data)  // Write current coefficient
{
  CF = Data;
}
void SerialReadLoop() {
  if (myPort.available() > 0)  // Check if there is data in the serial port and if the buffer is available
  {
    delay(55);
    SeriaDataLen = myPort.available();

    if (SeriaDataLen != 24) {
      while (myPort.read() >= 0) {}
      return;
    }
    for (byte a = 0; a < SeriaDataLen; a++)  // Get all bytes
    {
      SerialTemps[a] = myPort.read();
    }
    if (SerialTemps[1] != 0x5A)  // Mark recognition, discard if not
    {
      while (myPort.read() >= 0) {
      }
      return;
    }
    if (Checksum() == false)  // Check if checksum is correct
    {
      return;
    }
    SerialRead = 1;  // If passed above tests, data packet should be fine
    VolPar = ((uint32_t)SerialTemps[2] << 16) + ((uint32_t)SerialTemps[3] << 8) + SerialTemps[4];  // Get voltage parameter
    if (bitRead(SerialTemps[20], 6) == 1)  // If voltage register refreshes, get data
    { VolData = ((uint32_t)SerialTemps[5] << 16) + ((uint32_t)SerialTemps[6] << 8) + SerialTemps[7]; }  // Get voltage register
    CurrentPar = ((uint32_t)SerialTemps[8] << 16) + ((uint32_t)SerialTemps[9] << 8) + SerialTemps[10];  // Current parameter
    if (bitRead(SerialTemps[20], 5) == 1)  // If current register updates, get data
    { CurrentData = ((uint32_t)SerialTemps[11] << 16) + ((uint32_t)SerialTemps[12] << 8) + SerialTemps[13]; }  // Current
    PowerPar = ((uint32_t)SerialTemps[14] << 16) + ((uint32_t)SerialTemps[15] << 8) + SerialTemps[16];  // Power parameter
    if (bitRead(SerialTemps[20], 4) == 1)  // If power register data updates, get data
    { PowerData = ((uint32_t)SerialTemps[17] << 16) + ((uint32_t)SerialTemps[18] << 8) + SerialTemps[19]; }  // Power data
    PF = ((uint32_t)SerialTemps[21] << 8) + SerialTemps[22];  // Pulse count register
    if (bitRead(SerialTemps[20], 7) == 1)  // Check if PF carry register carries, increment if carry
    {
      PFData++;
    }
  }
}
float GetVol()  // Get voltage
{
  float Vol = GetVolAnalog() * VF;  // Calculate effective voltage
  return Vol;
}
float GetVolAnalog()  // Get voltage ADC value
{
  float FVolPar = VolPar;  // float calculation
  float Vol = FVolPar / VolData;
  return Vol;  // Return manufacturer corrected ADC voltage value
}
float GetCurrent()  // Get effective current
{
  float Current = GetCurrentAnalog() * CF;  // Calculate effective current
  return Current;
}
float GetCurrentAnalog()  // Get current manufacturer corrected ADC raw value
{
  float FCurrentPar = CurrentPar;
  float Current = FCurrentPar / (float)CurrentData;
  return Current;
}
// Calculate active power
float GetActivePower() {
  float FPowerPar = PowerPar;
  float FPowerData = PowerData;
  float Power = FPowerPar / FPowerData * VF * CF;  // Calculate active power
  return Power;
}
bool Checksum()  // Checksum test
{
  byte check = 0;
  for (byte a = 2; a <= 22; a++) {
    check = check + SerialTemps[a];
  }
  if (check == SerialTemps[23]) {
    return true;
  } else {
    return false;  // Checksum fails
  }
}
void readData() {

  Active_Volt = GetVol();
  Serial.print("Get_Vol: ");
  Serial.printf("%.2f", Active_Volt);  // Voltage
  Serial.println(" V");

  Active_Current = GetCurrent();
  Serial.print("Get_I: ");
  Serial.printf("%.3f", Active_Current);  // Current
  Serial.println(" A");

  Active_Power = GetActivePower();
  Serial.print("GetActivePower: ");
  Serial.print(Active_Power);  // Effective power
  Serial.println(" W");

  // Output energy consumed
  Serial.print("Total Energy Consumed: ");
  Serial.print(totalEnergyConsumed);
  Serial.println(" kWh");

  unsigned long currentTime = millis();
  if (currentTime - lastSendTime >= sendInterval) {
    if (mqttClient.connect(deviceId, pubId, password)) {
      String messageContent = "{ \"id\": \"123\", \"params\": {\"Active_Volt\": {\"value\": \"" + String(Active_Volt, 2) + "\"}, \"Active_Current\": {\"value\": \"" + String(Active_Current, 3) + "\"}, \"Active_Power\": {\"value\": \"" + String(Active_Power, 2) + "\"} } }";
      Serial.println(messageContent);
      mqttClient.publish(dataTopic, messageContent.c_str());
    }
    lastSendTime = currentTime;
  }
}

void setup() {
  Serial.begin(115200);

  WiFi.begin("liuxiang", "12345678");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  mqttClient.setServer(mqtt_server, mqtt_port);
  mqttClient.setCallback(callback);

  Blinker.begin(auth, ssid, pswd);
  Blinker.attachHeartbeat(heartbeat);
  pinMode(32, OUTPUT);
  Serial.println("Hardserialinit over...");
  myPort.begin(4800, SWSERIAL_8E1, MYPORT_RX, MYPORT_TX, false);  // 4800 baud rate, even parity
  if (!myPort) {                                                  // If the object did not initialize, then its configuration is invalid
    Serial.println("Invalid EspSoftwareSerial pin configuration, check config");
    while (1) {  // Don't continue with invalid configuration
    }
  }
  delay(1000);
  setVF(1.88);
  setCF(1);
  Serial.println("init over...");
  delay(1000);
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();
}
void loop() {
  Blinker.run();
  digitalWrite(32, HIGH);
  SerialReadLoop();
  if (SerialRead == 1) {
    SerialRead = 0;
    readData();
    // Update energy consumed here
    if (Active_Power > 0) {
      totalEnergyConsumed += static_cast<double>(Active_Power) / 3600.0;  // Use double type for cumulative operation
    }
  }
}

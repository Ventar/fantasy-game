#include <Arduino.h>
#include <CustomNeoPixel.h>
#include <CustomWiFiManager.h>
#include <SensorModule.h>
#include <UDPService.h>

SensorModule *modA;

CustomNeoPixel *strip;

CustomWiFiManager *wifiManager;

UDPService *udpService;
IPAddress *gameServerAddress;
uint16_t gameServerPort;

void edgeSensorUpdated(SensorModule *mod) {
    Serial.println("An EDGE sensor update was triggered...");
    uint32_t c = random(5, 14);
    Serial.println(c);
    strip->setColor(Color[c]);
}
void boardSensorUpdated(SensorModule *mod) {
    Serial.println("A BOARD sensor update was triggered...");
    uint32_t c = random(14, 20);
    Serial.println(c);
    strip->setColor(Color[c]);
}
void buttonSensorUpdated(SensorModule *mod) {
    Serial.println("A BUTTON sensor update was triggered...");
    uint32_t c = random(20, 34);
    Serial.println(c);
    strip->setColor(Color[c]);
}

void connectToWifi() {

    Serial.println("Setup WiFi...");
    strip->setColor(strip->Color(0, 128, 0));

    wifiManager = new CustomWiFiManager(
        "War Of Elements", [](WiFiManager *wm) { strip->setColor(strip->Color(255, 0, 0)); }, [](WiFiManager *wm) { strip->setColor(strip->Color(0, 0, 0)); });

    modA->setButtonCallback([](SensorModule *mod) { wifiManager->resetSettings(); });

    for (byte i = 0; i < 20; i++) {
        delay(100);
        modA->checkIRQ();
    }

    wifiManager->connect();
}

void setup() {

    Serial.begin(115200);

    while (!Serial) {
        delay(10);
    }

    Serial.println("\n\nSmart Board 0.0.6\n");

    Wire.begin(17,16); // Initalize the I2C Bus

    strip = new CustomNeoPixel(16, 22); // create a new LED strip
    modA = new SensorModule(&Wire, 32); // create a new sensor module

    connectToWifi(); // connect to WiFI

    // Set the callbackes, this is done in the connectToWifi() method to reset WiFi, so we have to wait until the connection is established before a switch to
    // the game mode
    modA->setBoardCallback(&boardSensorUpdated);
    modA->setEdgeCallback(&edgeSensorUpdated);
    modA->setButtonCallback(&buttonSensorUpdated);

    udpService = new UDPService("sbmodule", 4669);

    udpService->on(
        UDP_MESSAGE_REGISTER, UDP_CALLBACK {
            Serial.println("Received REGISTER message...");
            gameServerAddress = new IPAddress(data[1], data[2], data[3], data[4]);
            gameServerPort = data[6] | data[5] << 8;

            Serial.printf("Set UDP server address to %s:%d", gameServerAddress->toString().c_str(), gameServerPort);
        });

    Serial.println("\n\nStarted Smart Board...");
}

void loop() {
    modA->checkIRQ();
    udpService->handleUDP();
}
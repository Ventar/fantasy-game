#include <Arduino.h>
#include <CustomNeoPixel.h>
#include <CustomWiFiManager.h>
#include <SensorModule.h>
#include <UDPService.h>

SensorModule *modA;
SensorModule *modB;

CustomNeoPixel *strip;

CustomWiFiManager *wifiManager;

UDPService *udpService;
IPAddress *gameServerAddress;
uint16_t gameServerPort;

TaskHandle_t udpTask;

uint32_t currentColor;

void edgeSensorUpdated(SensorModule *mod) {
    Serial.println("An EDGE sensor update was triggered...");
    if (currentColor == Color[Orange]) {
        currentColor = Color[Red];
    } else {
        currentColor = Color[Orange];
    }
    strip->setColor(currentColor);
}
void boardSensorUpdated(SensorModule *mod) {
    Serial.println("A BOARD sensor update was triggered...");
    if (currentColor == Color[DarkGreen]) {
        currentColor = Color[LightGreen];
    } else {
        currentColor = Color[DarkGreen];
    }
    strip->setColor(currentColor);
}
void buttonSensorUpdated(SensorModule *mod) {
    Serial.println("A BUTTON sensor update was triggered...");
    if (currentColor == Color[DarkBlue]) {
        currentColor = Color[SkyBlue];
    } else {
        currentColor = Color[DarkBlue];
    }

    strip->setColor(currentColor);
}

void setupWiFi() {

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

void setupUDPMessageHandler() {
    udpService = new UDPService("sbmodule", 4669);

    udpService->on(
        UDP_MESSAGE_REGISTER, UDP_CALLBACK {
            Serial.println("Received REGISTER message...");
            gameServerAddress = new IPAddress(data[1], data[2], data[3], data[4]);
            gameServerPort = data[6] | data[5] << 8;

            Serial.printf("Set UDP server address to %s:%d", gameServerAddress->toString().c_str(), gameServerPort);
        });
    udpService->on(
        UDP_MESSAGE_CLEAR_COLORS, UDP_CALLBACK {
            Serial.println("Received CLEAR_COLOR message...");
            strip->clear();
            strip->show();
        });
}

void UDPTaskCode(void *parameter) {
    for (;;) {
        udpService->handleUDP();
    }
}

void setup() {

    Serial.begin(115200);

    while (!Serial) {
        delay(10);
    }

    Serial.println("\n\nSmart Board 0.0.6\n");

    Wire.begin(17, 16); // Initalize the I2C Bus

    strip = new CustomNeoPixel(32, 22);          // create a new LED strip
    modA = new SensorModule(&Wire, 32, 0x70, 0); // create a new sensor module
    modA->enableEdgeSensors(false);
    modA->enableBoardSensors(false);
    //modA->enableButtonSensors(false);
    modB = new SensorModule(&Wire, 25, 0x70, 1); // create a new sensor module
    modB->enableEdgeSensors(false);
    modB->enableBoardSensors(false);
    //modB->enableButtonSensors(false);

    setupWiFi(); // connect to WiFI

    // Set the callbackes, this is done in the connectToWifi() method to reset WiFi, so we have to wait until the connection is established before a switch to
    // the game mode
    modA->setBoardCallback(&boardSensorUpdated);
    modA->setEdgeCallback(&edgeSensorUpdated);
    modA->setButtonCallback(&buttonSensorUpdated);
    modB->setBoardCallback(&boardSensorUpdated);
    modB->setEdgeCallback(&edgeSensorUpdated);
    modB->setButtonCallback(&buttonSensorUpdated);

    setupUDPMessageHandler();

    Serial.print("setup() running on core ");
    Serial.println(xPortGetCoreID());

    xTaskCreatePinnedToCore(UDPTaskCode, /* Function to implement the task */
                            "UDP",       /* Name of the task */
                            10000,       /* Stack size in words */
                            NULL,        /* Task input parameter */
                            0,           /* Priority of the task */
                            &udpTask,    /* Task handle. */
                            0);          /* Core where the task should run */

    Serial.println("\n\nStarted Smart Board...");
}

void loop() {
    modA->checkIRQ();
    modB->checkIRQ();
    delay(5000);
    Serial.println("\nModule A");
    modA->dumpPinStatesToSerial();
    Serial.println("\nModule B");
    modB->dumpPinStatesToSerial();
    Serial.println("\n-----------------------------------------------");
}
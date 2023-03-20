#include <Arduino.h>
#include <Board.h>
#include <CustomNeoPixel.h>
#include <CustomWiFiManager.h>
#include <UDPMessageHandler.h>

Board *board;

CustomWiFiManager *wifiManager;

UDPMessageHandler *udpService;
IPAddress *gameServerAddress;
uint16_t gameServerPort;

TaskHandle_t udpTask;

uint32_t currentColor;

void edgeSensorUpdated();
void boardSensorUpdated();
void buttonSensorUpdated();

void UDPTaskCode(void *parameter) {
    for (;;) {
        udpService->update();
    }
}

void setup() {

    Serial.begin(115200);

    while (!Serial) {
        delay(10);
    }

    Serial.println("\n\nSmart Board 0.0.6\n");

    Wire.begin(17, 16); // Initalize the I2C Bus

    board = new Board(&Wire, 22, 0x70, 21, 25, 18, 19);
    board->setCallback(SensorModule::BOARD, &boardSensorUpdated);
    board->setCallback(SensorModule::EDGE, &edgeSensorUpdated);
    board->setCallback(SensorModule::BUTTON, &buttonSensorUpdated);
    board->strip->setColor(Color[LightGreen]);

    wifiManager = new CustomWiFiManager(
        "War Of Elements", [](WiFiManager *wm) { board->strip->setColor(Color[Red]); }, [](WiFiManager *wm) { board->strip->setColor(Color[Black]); });

    udpService = new UDPMessageHandler(board);

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

void loop() { board->checkIRQ(); }

void edgeSensorUpdated() {
    if (currentColor == Color[Orange]) {
        currentColor = Color[Red];
    } else {
        currentColor = Color[Orange];
    }

    udpService->sendSensorUpdate(SensorModule::EDGE);

    board->strip->setColor(currentColor);
}
void boardSensorUpdated() {
    if (currentColor == Color[DarkGreen]) {
        currentColor = Color[LightGreen];
    } else {
        currentColor = Color[DarkGreen];
    }

    udpService->sendSensorUpdate(SensorModule::BOARD);

    board->strip->setColor(currentColor);
}
void buttonSensorUpdated() {
    if (currentColor == Color[DarkBlue]) {
        currentColor = Color[AquaMarine];
    } else {
        currentColor = Color[DarkBlue];
    }

    udpService->sendSensorUpdate(SensorModule::BUTTON);

    board->strip->setColor(currentColor);
}
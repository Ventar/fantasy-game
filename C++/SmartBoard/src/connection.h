#include <ESP8266WiFi.h>
#include <ESP8266mDNS.h>
#include <WiFiUdp.h>
#include <neopixel.h>

const char *NB_WIFI_SSID = "HouseOfTeens";
const char *NB_WIFI_PASSWORD = "8882941015907883";

/**
 * The UDP server to receive messages from the neoboard game server
 */
WiFiUDP Udp;
const char *NB_MDNS_SERVICE = "sbmodule";
const uint16_t NB_UDP_PORT = 4000;
uint8_t incomingPacket[512];
/**
 * @brief The address of the game server
 * 
 */
IPAddress serverAddress;
/**
 * @brief The port of the game server.
 * 
 */
int serverPort;

/**
 * @brief Sends an update about the sensor status to the game server.
 * 
 */
void sendStatusUodate(BoardRotation rotation)
{
    if (!serverAddress) {
        Serial.println("No server configured, do not send status update");
        return;
    }

    String name = WiFi.macAddress();
    name.replace(":", "");

    uint8_t outgoingPacket[13 + 3 * FIELD_COUNT * FIELD_COUNT];
    outgoingPacket[0] = 0;

    for (int i = 0; i < 12; i++) {
        outgoingPacket[i + 1] = name.charAt(i);
    }

    int record = 13;

    for (int row = 0; row < FIELD_COUNT; row++) {
        for (int column = 0; column < FIELD_COUNT; column++) {

            SmartField *field = getField(rotation, column, row);
            uint8_t sensorMask = field->west.enabled;
            sensorMask |= field->south.enabled << 1;
            sensorMask |= field->east.enabled << 2;
            sensorMask |= field->north.enabled << 3;

            outgoingPacket[record] = column;
            outgoingPacket[record + 1] = row;
            outgoingPacket[record + 2] = sensorMask;

            record += 3;
        }
    }

    // Serial.print("Record counter: ");
    // Serial.println(record);

    /*
    for (int i = 0; i < 1 + 3 * FIELD_COUNT * FIELD_COUNT; i++) {
        Serial.print(outgoingPacket[i]);
        Serial.println("");
    }
    */

    Udp.beginPacket(serverAddress, serverPort);
    Udp.write(outgoingPacket, record);
    Udp.endPacket();
}

void handleUDP(BoardRotation rotation)
{
    int packetSize = Udp.parsePacket();

    if (packetSize) {
        Serial.printf("Received %d bytes from %s, port %d\n", packetSize, Udp.remoteIP().toString().c_str(), Udp.remotePort());
        int len = Udp.read(incomingPacket, 512);

        if (len > 0) {
            incomingPacket[len] = '\0';
        }

        /*
        for (int i = 0; i < packetSize; i++) {
            Serial.print(incomingPacket[i]);
            Serial.println("");
        }
        */

        switch (incomingPacket[0]) {
        case 0: {

            Serial.println("command: REGISTER_LISTENER");
            serverAddress = IPAddress(incomingPacket[1], incomingPacket[2], incomingPacket[3], incomingPacket[4]);
            serverPort = incomingPacket[6] | incomingPacket[5] << 8;

            Serial.print("Set server IP address to ");
            Serial.print(serverAddress);
            Serial.print(":");
            Serial.println(serverPort);

            sendStatusUodate(rotation);

            readGroups(rotation, true);
            break;
        }
        case 1: {
            Serial.printf("command: SHOW_PIXEL\n");

            int recordSize = 8;
            for (int i = 0; i < 36; i++) {

                int column = incomingPacket[1 + (i * recordSize) + 0];
                int row = incomingPacket[1 + (i * recordSize) + 1];

                setFieldColor(
                    rotation,
                    column,
                    row,
                    incomingPacket[1 + (i * recordSize) + 2],
                    incomingPacket[1 + (i * recordSize) + 3],
                    incomingPacket[1 + (i * recordSize) + 4]);
                getField(rotation, column, row)->effect = (ColorEffect) incomingPacket[1 + (i * recordSize) + 5];
            }

            strip.show();
            readGroups(rotation, true);
            break;
        }
        case 2: {
            Serial.printf("command: RESET_PIXEL\n");
            for (int i = 0; i < strip.numPixels(); i++) {
                strip.setPixelColor(i, 0);
            }
            strip.show();
            readGroups(rotation, true);
            break;
        }
        case 3: {
            Serial.printf("command: SET_BRIGHTNESS to %d\n", incomingPacket[1]);
            strip.setBrightness(incomingPacket[1]);
            strip.show();
        }
        case 4: {
            Serial.printf("command: CALIBRATE_SENSORS\n");
            readGroups(rotation, true);
        }
        }
    }
}

/**
 * Setup the WiFi Module for the communication
 */
void setupConnectivity()
{

#ifdef DEBUG_BOARD
    Serial.println("Debug mode, skip WiFi setup...");
#endif

#ifndef DEBUG_BOARD

    // Setup WiFI
    // ----------------------------------------------------------------------------

    WiFi.begin(NB_WIFI_SSID, NB_WIFI_PASSWORD);

    Serial.print("\nConnecting...");

    while (WiFi.status() != WL_CONNECTED) {
        delay(1000);
        Serial.print(".");
    }

    Serial.printf("\nConnected ip ::= [%s]...", WiFi.localIP().toString().c_str());
    Serial.printf("\nConnected MAC ::= [%s]...", WiFi.macAddress().c_str());

    WiFi.macAddress().replace(":", "");

    // Setup MDNS for automatic discovery of the board.
    // ----------------------------------------------------------------------------

    String name = WiFi.macAddress();
    name.replace(":", "");

    if (!MDNS.begin(name)) {
        Serial.println("Error setting up MDNS responder!");
    }

    Serial.print("\nSetup MDNS module name ::= [" + name + "], service ::= [" + NB_MDNS_SERVICE + "]");

    MDNS.addService(NB_MDNS_SERVICE, "udp", NB_UDP_PORT);

    // Setup the UDP server
    // ----------------------------------------------------------------------------
    Udp.begin(NB_UDP_PORT);
    Serial.printf("\nSetup UDP server on port ::= [%d]\n", NB_UDP_PORT);

#endif
}

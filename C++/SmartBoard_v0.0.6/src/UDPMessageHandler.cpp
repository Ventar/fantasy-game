
#include <UDPMessageHandler.h>

#define REGISTER 0
#define BOARD_COLOR_CLEAR 1
#define BOARD_COLOR_UPDATE 2
#define BOARD_ENABLE_SENSOR 3
#define BOARD_SET_BRIGHTNESS 4

UDPMessageHandler::UDPMessageHandler(Board *board)
    : _board(board), UDPConnection("sbmodule", 4669){

                     };

void UDPMessageHandler::handleMessage(uint8_t *packet) {

    switch (packet[0]) {
    case REGISTER: {
        _gameServerAddress[0] = packet[1];
        _gameServerAddress[1] = packet[2];
        _gameServerAddress[2] = packet[3];
        _gameServerAddress[3] = packet[4];
        _gameServerPort = packet[6] | packet[5] << 8;

        Serial.printf("Set UDP server address to %s:%d\n\n", IPAddress(_gameServerAddress).toString().c_str(), _gameServerPort);

        break;
    }
    case BOARD_COLOR_CLEAR: {
        _board->strip->clear();
        _board->strip->show();
        Serial.printf("Clear all board colors...\n\n", IPAddress(_gameServerAddress).toString().c_str(), _gameServerPort);
        break;
    }
    case BOARD_COLOR_UPDATE: {

        for (uint8_t i = 0; i < packet[1]; i++) { // number of leds to update
            _board->strip->setPixelColor(packet[2 + i * 2], Color[packet[3 + i * 2]]);
            Serial.printf("Set led ::= [%d] to := %s(%d)...\n", packet[2 + i * 2], GetColorName(packet[3 + i * 2]).c_str(), packet[3 + i * 2]);
        }
        Serial.println("");

        _board->strip->show();

        break;
    }
    case BOARD_ENABLE_SENSOR: {

        // byte mask = 1 << 2;              Set the third bit of a byte to one and use the mask 0000 0100
        // bool board = packet[1] & mask;   perform an and to check if the value in the byte has a one as value, e.g.
        //                                  0000 0001 & 0000 0100 would lead to an false and
        //                                  0000 0100 & 0000 0100 would lead to an true value

        bool board = packet[1] & (1 << 2);
        bool button = packet[1] & (1 << 1);
        bool edge = packet[1] & 1;

        Serial.printf("Update sensor usage, board ::= [%d], button ::= [%d], edge ::= [%d]\n\n", board, button, edge);

        _board->enableSensors(button, board, edge);

        break;
    }
    case BOARD_SET_BRIGHTNESS: {
        Serial.printf("Set brightness to == [%d]", packet[1]);
        _board->strip->setBrightness(packet[1]);
        _board->strip->show();
        break;
    }
    }
}

void UDPMessageHandler::sendSensorUpdate(SensorModule::SensorType type) {
    byte packet[40] = {0};
    WiFi.macAddress(packet); // add the mac address to the first 6 bytes of packet.

    // for (int i = 0; i < 6; i++) {
    //     Serial.printf("%02x:", packet[i]);
    // }

    packet[6] = 1;    // Device type is BOARD
    packet[7] = type; // Event type matches the number of the sensor type, i.e.
                      // 0 = BUTTON
                      // 1 = BOARD
                      // 2 = EDGE

    _board->writeSensorState(type, &packet[8]);

    Serial.print("[ ");
    for (uint8_t i = 0; i < 40; i++) {
        Serial.print(packet[i]);
        if (i < 39)
            Serial.print(", ");
    }
    Serial.println("]");

    if (_gameServerAddress) {
        Serial.printf("Sent packet to %s:%d\n\n", IPAddress(_gameServerAddress).toString().c_str(), _gameServerPort);
        _udp->beginPacket(IPAddress(_gameServerAddress), _gameServerPort);
        _udp->write(packet, 40);
        _udp->endPacket();
    }
}

#include "Board.h"

#define MODULES 4
#define FOR_EVERY_MODULE for (uint8_t i = 0; i < MODULES; i++)

Board::Board(TwoWire *i2cBus, uint8_t gpioLed, uint8_t mxAddress, uint8_t gpioModA, uint8_t gpioModB, uint8_t gpioModC, uint8_t gpioModD) {

    strip = new CustomNeoPixel(64, gpioLed);
    

    modules[0] = new SensorModule(&Wire, gpioModA, mxAddress, 0);
    modules[1] = new SensorModule(&Wire, gpioModB, mxAddress, 1);
    modules[2] = new SensorModule(&Wire, gpioModC, mxAddress, 2);
    modules[3] = new SensorModule(&Wire, gpioModD, mxAddress, 3);

    begin();
}

void Board::begin() {
    FOR_EVERY_MODULE
    modules[i]->begin();
}

void Board::checkIRQ() {
    FOR_EVERY_MODULE
    modules[i]->checkIRQ();
}

void Board::enableSensors(bool button, bool board, bool edge) {
    FOR_EVERY_MODULE
    modules[i]->enableSensors(button, board, edge);
};

void Board::setCallback(SensorModule::SensorType type, SensorUpdatedFunction callback) {
    FOR_EVERY_MODULE
    modules[i]->setCallback(type, callback);
}

void Board::writeSensorState(SensorModule::SensorType type, uint8_t *dest) {
    FOR_EVERY_MODULE
    switch (type) {
    case SensorModule::BUTTON:
    case SensorModule::BOARD:
        modules[i]->writeSensorState(type, &dest[i * 2]);
        break;
    case SensorModule::EDGE:
        modules[i]->writeSensorState(type, &dest[i * 8]);
        break;
    }
}
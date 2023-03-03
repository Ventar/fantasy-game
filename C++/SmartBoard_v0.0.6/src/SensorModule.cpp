#include <SensorModule.h>

#define PCA9555_REG_INPUT 0
#define PCA9555_REG_OUTPUT 2
#define PCA9555_REG_INVERT 4
#define PCA9555_REG_CONFIG 6

#define I2C_START 0x20
#define I2C_BUTTONS 0x20
#define I2C_SECTOR_0 0x21
#define I2C_SECTOR_1 0x22
#define I2C_SECTOR_2 0x23
#define I2C_SECTOR_3 0x24
#define I2C_BOARD 0x25
#define I2C_IRQ 0x26

#define CALLBACK_DELAY 100

SensorModule::SensorModule(TwoWire *i2cBus, uint8_t irq, uint8_t mxAddress, uint8_t mxChannel)
    : _i2cBus(i2cBus), _irq(irq), _mxAddress(mxAddress), _mxChannel(mxChannel) {
    begin();
}

bool SensorModule::begin() {
    pinMode(_irq, INPUT); // interrupt for multiplexer

    // the board module uses 7 PCA9555 with different addresses all in INPUT
    // mode. The code will setup the configuration registers of all modules and
    // request the current state of all sensors.

    uint8_t error = false;

    for (byte address = I2C_START; address < 0x27; address++) {
        error &= initMultiplexer(address);
    }

    return error;
}

void SensorModule::checkIRQ() {
    // check if the interrupt was triggered. The PCA9555 interrupt will change to LOW when a port / pin changes its state. A reset is performed when the
    // register is read the next time. The interrupt that is checked should be the one of the interrupt PCA9555 with address IC2_IRQ
    if (digitalRead(_irq) == 0) {
        Serial.printf("IRQ channel %d was triggered...\n", _mxChannel);
        // wait in case other IRQs are triggered, that may happen until the player has finalize the positioning of a tile on the board
        readMXPins(I2C_IRQ);

        // we only need to perform a read operation if a callback was set and the sensorupdate is enabled.
        if (_boardEnabled && sensorActive(I2C_IRQ, 8) && _boardCallback) {
            readMXPins(I2C_BOARD);
            dumpPinStatesToSerial(I2C_BOARD);
            _boardCallback(this);
        }

        if (_buttonEnabled && sensorActive(I2C_IRQ, 10) && _buttonCallback) {
            uint16_t oldVal = _mxPinStates[0];
            readMXPins(I2C_BUTTONS);

            // we only want to trigger an event once, when a button was pressed. Releasing a button should not be detected
            if (oldVal == 0) {
                _buttonCallback(this);
            }
        }

        if (_edgeEnabled && _edgeCallback && (sensorActive(I2C_IRQ, 0) || sensorActive(I2C_IRQ, 7) || sensorActive(I2C_IRQ, 9) || sensorActive(I2C_IRQ, 15))) {

            if (sensorActive(I2C_IRQ, 0)) {
                readMXPins(I2C_SECTOR_0);
            }
            if (sensorActive(I2C_IRQ, 7)) {
                readMXPins(I2C_SECTOR_1);
            }
            if (sensorActive(I2C_IRQ, 15)) {
                readMXPins(I2C_SECTOR_2);
            }
            if (sensorActive(I2C_IRQ, 9)) {
                readMXPins(I2C_SECTOR_3);
            }

            _edgeCallback(this);
        }
    }
}

uint16_t SensorModule::readMXPins(uint8_t address) {
    _mxPinStates[address - 0x20] = readRegister(address, PCA9555_REG_INPUT);
    _mxPinStates[address - 0x20] |= readRegister(address, PCA9555_REG_INPUT + 1) << 8;
    return _mxPinStates[address - 0x20];
}

bool SensorModule::sensorActive(uint8_t address, uint8_t pin) {
    // multiplexer for HAL sensors and irq one have state HIGH if no magnet was
    // detected (or no interrupt), the one for the button is the other way
    // arround...yes I know, bad design decision especially beacause I designed
    // the hardware and the software. Maybe I change it in a future prototype
    // but for now I am to lazy.

    if ((_mxPinStates[address - 0x20] & (1 << pin)) > 0) {
        return address != I2C_BUTTONS ? false : true;
    } else {
        return address != I2C_BUTTONS ? true : false;
    }
}

void SensorModule::dumpPinStatesToSerial(uint8_t address) {

    Serial.print("0x");
    Serial.print(address < 16 ? "0" : "");
    Serial.print(address, HEX);
    Serial.print(" ");

    for (uint8_t pin = 0; pin < 16; pin++) {

        Serial.print(" ");
        Serial.print(sensorActive(address, pin));
        Serial.print(" ");
    }

    Serial.println(" ");
}

void SensorModule::dumpPinStatesToSerial() {
    Serial.println("       0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15");

    for (byte address = I2C_START; address < 0x27; address++) {
        dumpPinStatesToSerial(address);
    }
}

// ---------------------------------------------------------------------------------------------------------------------------------
// low level interactions with the digital multiplexer
// ---------------------------------------------------------------------------------------------------------------------------------

bool SensorModule::initMultiplexer(uint8_t address) {

    // Configure the multiplexer to interact with the I2C bus of this module
    enableMXChannel();

    // first we test if a connection to the PCA9555 can be established by
    // sending a test byte. If at least one of the modules fail, we end the
    // setup
    _i2cBus->beginTransmission(address);
    _i2cBus->write(0x02); // Test Address
    uint8_t error = _i2cBus->endTransmission();

    if (error != 0) {
        return false;
    }

    // the IRQ multiplexer is not fully used, i.e. we set the not used ports to output to avoid random states and interrupts
    if (address != I2C_IRQ) {
        writeRegister(address, PCA9555_REG_CONFIG, 0xFF);
        writeRegister(address, PCA9555_REG_CONFIG + 1, 0xFF);
    } else {
        writeRegister(address, PCA9555_REG_CONFIG, 0x81);
        writeRegister(address, PCA9555_REG_CONFIG + 1, 0x87);
        // writeRegister(address, PCA9555_REG_CONFIG, 0x0);
        // writeRegister(address, PCA9555_REG_CONFIG + 1, 0x0);
    }

    // we need to read the values here because it is possible that some magnets
    // are present on the field, i.e. we cannot assume a clear setup
    readMXPins(address);

    return true;
}

// ATTENTION: This method does not use the enableMXChannel method for performance reasons. If it is used outside of the initMultiplexer() method the
// enableMXChannel method must be called upfront.
bool SensorModule::writeRegister(uint8_t address, uint8_t reg, uint8_t value) {
    // write output register to chip
    _i2cBus->beginTransmission(address); // setup direction registers
    _i2cBus->write(reg);                 // pointer to configuration register address 0
    _i2cBus->write(value);               // write config register low byte

    return _i2cBus->endTransmission();
}

uint16_t SensorModule::readRegister(uint8_t address, uint8_t reg) {

    // Configure the multiplexer to interact with the I2C bus of this module
    enableMXChannel();

    uint16_t _inputData;

    // read the address input register
    _i2cBus->beginTransmission(address); // setup read registers
    _i2cBus->write(reg);
    uint8_t _error = _i2cBus->endTransmission();

    // ask for 2 bytes to be returned
    if (_error != 0 || _i2cBus->requestFrom((int)address, 1) != 1) {
        // we are not receing the bytes we need
        return 256; // error code is above normal data range
    };

    // read both bytes
    _inputData = _i2cBus->read();
    return _inputData;
}

void SensorModule::enableMXChannel() {
    Wire.beginTransmission(_mxAddress);
    Wire.write(1 << _mxChannel);
    Wire.endTransmission();
};

#ifndef SENSOR_MODULE_H_
#define SENSOR_MODULE_H_

#include <Arduino.h>
#include <Wire.h>

#define BOARD_MODULE_DEBUG

class SensorModule; // forward declaration for the update function

/**
 * Update function that is triggered when a sensor state has changed.
 */
typedef void (*SensorUpdatedFunction)(SensorModule *module);

enum SensorType { NORTH, EAST, SOUTH, WEST, BOARD, BUTTON };

/**
 * Struct to hold information about a single sensor on the PCBs.
 */
struct SensorState {
    uint16_t type : 3;
    uint16_t state : 1;
};

/**
 * Represents a physical board module. The module is split into sectors, fields and sensors. The following image shows the sector and field view.
 *
 *   ┌─────────┬─────────┬─────────┬─────────┐
 *   │         │         │         │         │
 *   │   S2F2  │   S2F3  │   S3F2  │   S3F3  │
 *   │         │         │         │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │         │
 *   │   S2F0  │   S2F1  │   S3F0  │   S3F1  │
 *   │         │         │         │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │         │
 *   │   S0F2  │   S0F3  │   S1F2  │   S1F3  │
 *   │         │         │         │         │
 *   ├─────────┼─────────┼─────────┼─────────┤
 *   │         │         │         │         │
 *   │   S0F0  │   F0F1  │   S1F0  │   S1F1  │
 *   │         │         │         │         │
 *   └─────────┴─────────┴─────────┴─────────┘
 *
 *   Each field has 5 sensors to detect magnetic fields, one for each edge of the field and one in the upper left corner. The four
 *   sensors at the edge have the name of cardinal directions, the last one is labeled as BOARD one.
 *    ┌─────────────────────┐
 *    │ B      NORTH        │
 *    │         ███         │
 *    │ W                 E │
 *    │ E █     S0F0    █ A │
 *    │ S █             █ S │
 *    │ T                 T │
 *    │         ███         │
 *    │        SOUTH        │
 *    └─────────────────────┘
 *
 *  Finally every field has a button assigned that can be pressed by the player to interact with the module in a mechanical way. This
 *  sensor is labeld as button.
 *
 *  Based on these information every sensor on the field can be identified by the coordinates and the sensor type, e.g.
 *
 *  S0F0_NORTH
 *  S1F3_BUTTON
 *  ect.
 */
class SensorModule {
  public:
    /**
     * Creates a new instance.
     *
     * @param i2cBus I2C bus used to control the PCA9555 instances. Since the bus maybe shared with other instances it has to be provided from outside.
     * @param irq    the interrupt used for the complete gameboard. Used to update the internal multiplexer pin state array when a change is detected.
     * @param updateFunction function that is called when an sensor update was detected in the loop() function call
     */
    SensorModule(TwoWire *i2cBus, uint8_t irq);

    /**
     * Has to be executed before the use of this class. The i2cBus.begin(...) method has to be called BEFORE this method is called, otherwise the values will
     * not be resolved correctly
     */
    bool begin();

    void checkIRQ();

    /**
     * Checks if the passed PIN is active (set to 0) for the passed multiplexer address. The readMXPins() method has to be executed before a call to this method
     * to have values that represent the actual state. This is usually performed when the interrupt is triggered.
     *
     * @param address  the address of the multiplexer to query a pin.
     * @param pin      the pin from 0 - 15 to query
     */
    bool sensorActive(uint8_t address, uint8_t pin);

    /**
     * Enables the detection and the execution of callbacks when a sensor update was recognized.
     * @param enabled true if the callback should be executed, false otherwise
     */
    void enableBoardSensors(bool enabled) { _boardEnabled = enabled; };

    /**
     * Enables the detection and the execution of callbacks when a sensor update was recognized.
     * @param enabled true if the callback should be executed, false otherwise
     */
    void enableEdgeSensors(bool enabled) { _edgeEnabled = enabled; };

    /**
     * Enables the detection and the execution of callbacks when a sensor update was recognized.
     * @param enabled true if the callback should be executed, false otherwise
     */
    void enableButtonSensors(bool enabled) { _buttonEnabled = enabled; };

    /*
     * Sets the callback function for a button sensor change. The callback is only triggered if the sensors are activated (default is active).
     * @param callback the callback to execute
     */
    void setButtonCallback(SensorUpdatedFunction callback) { _buttonCallback = callback; };

    /*
     * Sets the callback function for a edge sensor change. The callback is only triggered if the sensors are activated (default is active).
     * @param callback the callback to execute
     */
    void setEdgeCallback(SensorUpdatedFunction callback) { _edgeCallback = callback; };

    /*
     * Sets the callback function for a board sensor change. The callback is only triggered if the sensors are activated (default is active).
     * @param callback the callback to execute
     */
    void setBoardCallback(SensorUpdatedFunction callback) { _boardCallback = callback; };

  private:
    /**
     * Holds the state of the digital multiplexer ICs. This array is initialized during initalization when the initMultiplexer() method is called and updated
     * whenever the readMXPins(address) method for a given adress is called. That means that without an update the ICs may have different states than the ones
     * represented here.
     *
     * The board has 7 multiplexers with the following addresses
     *
     * I2C_BUTTONS    0x20
     * I2C_SECTOR_0   0x21
     * I2C_SECTOR_1   0x22
     * I2C_SECTOR_2   0x23
     * I2C_SECTOR_3   0x24
     * I2C_BOARD      0x25
     * I2C_IRQ        0x26
     *
     * which are stored in this array in increasing order (_mxPinStates[1] == I2C_SECTOR_0)
     *
     */
    uint16_t _mxPinStates[7];

    /**
     * I2C bus used to control the PCA9555 instances. Since the bus maybe shared with other instances it has to be provided from outside.
     */
    TwoWire *_i2cBus;

    /**
     * Interrupt for the complete board module
     */
    uint8_t _irq;

    /**
     * if the edge sensors of the fields are enabled.
     */
    bool _edgeEnabled = true;

    /**
     * If the button sensors of the fields are enabled.
     */
    bool _buttonEnabled = true;

    /**
     * if the board sensors of the fields are enabled.
     */
    bool _boardEnabled = true;

    /**
     * Function that is called when an sensor update was detected in the loop() function call for the edge sensors. If no callback function is set, the sensor
     * changes of these sensors are ignored.
     */
    SensorUpdatedFunction _edgeCallback;

    /**
     * Function that is called when an sensor update was detected in the loop() function call for the board sensors.
     */
    SensorUpdatedFunction _boardCallback;

    /**
     * Function that is called when an sensor update was detected in the loop() function call for the button sensors.
     */
    SensorUpdatedFunction _buttonCallback;

    // business logic to update the internal state of the board module instance
    // ---------------------------------------------------------------------------------------------------------------------------------

    /**
     * Reads the state of the pins for the multiplexer with the passed address. The result is stored in the _mxPinStates array for the passed address.
     *
     * @param address the I2C address of the queried PCA9555
     *
     * @return the current state
     */
    uint16_t readMXPins(uint8_t address);

    /**
     * Writes the physical PIN states of the passed multiplexer address to the serial output for debugging reasons. Used when BOARD_MODULE_DEBUG is defined
     * @param address the address of the I2C multiplexer to write the pins
     */
    void dumpPinStatesToSerial(uint8_t address);

    void dumpPinStatesToSerial();

    // low level interactions with the digital multiplexer
    // ---------------------------------------------------------------------------------------------------------------------------------

    /**
     * Performs the initialization of the multiplexer with the given address. During the initialization the I2C connection is tested and the configuration
     * register of the multiplexer is set to INPUT only. This method is called for all used multiplexers of the board.
     *
     * @param address the multiplexer address
     */
    bool initMultiplexer(uint8_t address);

    /**
     * Write the value given to the register set to selected chip.
     *
     * @param address Address of I2C chip
     * @param reg    register to write to
     * @param value    value to write to register
     *
     */
    bool writeRegister(uint8_t address, uint8_t reg, uint8_t value);

    /**
     * Reads the data from addressed chip at selected register.
     * If the value is above 255, an error is set.
     * error codes :  256 = either 0 or more than one byte is received from the chip
     *
     * @param address Address of I2C chip
     * @param reg    Register to read from
     * @return data in register
     */
    uint16_t readRegister(uint8_t address, uint8_t reg);
};

#endif

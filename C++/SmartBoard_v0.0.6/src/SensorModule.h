#ifndef SENSOR_MODULE_H_
#define SENSOR_MODULE_H_

#include <Arduino.h>
#include <Wire.h>

#define BOARD_MODULE_DEBUG

class SensorModule; // forward declaration for the update function

/**
 * Update function that is triggered when a sensor state has changed.
 */
typedef void (*SensorUpdatedFunction)();

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
     * @brief the type of sensor used
     */
    enum SensorType { BUTTON, BOARD, EDGE };

    /**
     * Creates a new instance.
     *
     * @param i2cBus I2C bus used to control the PCA9555 instances. Since the bus maybe shared with other instances it has to be provided from outside.
     * @param irq    the interrupt used for the complete gameboard. Used to update the internal multiplexer pin state array when a change is detected.
     * @param updateFunction function that is called when an sensor update was detected in the loop() function call
     * @param mxAddress the I2C address of the multiplexer to which this module is connected
     * @param mxChannel the I2C multiplexer channel to which this module is connected.
     */
    SensorModule(TwoWire *i2cBus, uint8_t irq, uint8_t mxAddress, uint8_t mxChannel);

    /**
     * Has to be executed before the use of this class. The i2cBus.begin(...) method has to be called BEFORE this method is called, otherwise the values will
     * not be resolved correctly
     */
    bool begin();

    void checkIRQ();

    /**
     * Performs an update of the internal registers of the module and triggers call backs if a change was detected.
     */
    void update();

    /**
     * Checks if the passed PIN is active (set to 0) for the passed multiplexer address. The readMXPins() method has to be executed before a call to this method
     * to have values that represent the actual state. This is usually performed when the interrupt is triggered.
     *
     * @param address  the address of the multiplexer to query a pin.
     * @param pin      the pin from 0 - 15 to query
     */
    bool sensorActive(uint8_t address, uint8_t pin);

    /**
     * Enables or disables the sensors of the physical board modules. If a sensor is disabled no events of the corresponding type will be sent by the board
     * anymore. This can be used to avoid error handling for irrelevant cases. If you wait for a button event and the player shouldn't add / remove / move
     * something on the game board, disabling the BOARD and EDGE sensors can avoid false positives during the interaction. In addition, disabled sensors reduce
     * the overall processing time of the physical board module and my increase the responsiveness of the hardware. <p> The method will always set the state of
     * all sensors, i.e. the full expected value set (all three boolean values) have to be provided, there is no delta calculation.
     *
     * @param button if the button sensor type is enabled or not
     * @param board if the board sensor type is enabled or not
     * @param edge if the edge sensor type is enabled or not
     */
    void enableSensors(bool button, bool board, bool edge) {
        _edgeEnabled = edge;
        _buttonEnabled = button;
        _boardEnabled = board;
    };

    /**
     * @brief Sets the callback function for a sensor change. The callback is only triggered if the sensors are activated (default is active).
     * @param callback the callback to execute
     */
    void setCallback(SensorType type, SensorUpdatedFunction callback);

    /**
     @brief  Writes the sensor state of the passed type as bit encoded bytes to the destination parameter. The size depends on the type, i.e. for board and
     button we need 2 bytes, every module of the board has 16 sensors so that 16 bits are needed, for edge sensors 4 bytes are set (every module has 16 fields with
     4 sensors)
    */
    void writeSensorState(SensorType type, uint8_t *dest);

    /**
     * Writes the physical PIN states of the passed multiplexer address to the serial output for debugging reasons. Used when BOARD_MODULE_DEBUG is defined
     * @param address the address of the I2C multiplexer to write the pins
     */
    void dumpPinStatesToSerial(uint8_t address);

    void dumpPinStatesToSerial();

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
     * The previous version of the pin states to calculate the delta between the current one and the previous one
     */
    uint16_t _mxPinStatesPrev[7];

    /**
     * I2C bus used to control the PCA9555 instances. Since the bus maybe shared with other instances it has to be provided from outside.
     */
    TwoWire *_i2cBus;

    /**
     * Interrupt for the complete board module
     */
    uint8_t _irq;

    /**
     * Address of the TCA9548 multiplexer to which the module is connected.
     */
    uint8_t _mxAddress;

    /**
     * Channel of the TCA9548 multiplexer to which the module is connected.
     */
    uint8_t _mxChannel;

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

    /**
     * Configures the TCA9548 to connect to the I2C bus of this module
     */
    void enableMXChannel();
};

#endif

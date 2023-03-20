#ifndef UDP_BOARD_H_
#define UDP_BOARD_H_

#include <Arduino.h>
#include <CustomNeoPixel.h>
#include <SensorModule.h>
#include <Wire.h>

/**
 * @brief A game board that is constructed from 4 sensor module instances.
 *
 */
class Board {

  public:
    
    /**
     * LED strip
     */
    CustomNeoPixel *strip;

    /**
     * @brief Creates a new board instance.
     * @param i2cBus I2C bus used to control the PCA9555 instances. Since the bus maybe shared with other instances it has to be provided from outside.
     * @param mxAddress the I2C address of the multiplexer to which this module is connected
     * @param gpioLed the gpio of the LED
     * @param gpioModA gpio interrupt for sensor module A
     * @param gpioModB gpio interrupt for sensor module B
     * @param gpioModC gpio interrupt for sensor module C
     * @param gpioModD gpio interrupt for sensor module D
     */
    Board(TwoWire *i2cBus, uint8_t gpioLed, uint8_t mxAddress, uint8_t gpioModA, uint8_t gpioModB, uint8_t gpioModC, uint8_t gpioModD);

    /**
     * Checks if one of the sensor modules was interrupted, i.e. if the state hs changed.
     */
    void checkIRQ();

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
    void enableSensors(bool button, bool board, bool edge);

    /**
     * @brief Sets the callback function for a sensor change. The callback is only triggered if the sensors are activated (default is active).
     * @param callback the callback to execute
     */
    void setCallback(SensorModule::SensorType type, SensorUpdatedFunction callback);

    /**
     @brief  Writes the sensor state of the passed type as bit encoded bytes to the destination parameter. The size depends on the type, i.e. for board and button 
      we need 8 bytes, every module of the board has 16 sensors and we have 4 modules, for edge sensors 16 bytes are set (every module has 16 fields with 4 sensors, i.e. we
     need 4 bytes to encode all sensor states and a board consists of 4 modules)
    */
    void writeSensorState(SensorModule::SensorType type, uint8_t *dest);

  private:
    /**
     * Sensor modules of the board
     */
    SensorModule *modules[4];

    /**
     * Has to be executed before the use of this class. The i2cBus.begin(...) method has to be called BEFORE this method is called, otherwise the values will
     * not be resolved correctly
     */
    void begin();
};

#endif
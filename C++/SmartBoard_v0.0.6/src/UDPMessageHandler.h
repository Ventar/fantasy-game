#ifndef UDP_MESSAGEHANDLER_H_
#define UDP_MESSAGEHANDLER_H_

#include <Arduino.h>
#include <Board.h>
#include <UDPConnection.h>

class UDPMessageHandler;

/**
 * @brief message handler to handle incoming UDP packages.
 */
class UDPMessageHandler : public UDPConnection {
    
  public:
    /**
     * @brief creates a service instance and starts the MDNS service.
     *
     * @param   mdnsName the MDNS service name that is used by this device
     * @param   udpPort  the UDP port that is used by this device
     */
    UDPMessageHandler(Board *board);

    /**
     * @brief logic to handle incoming packets
     * @param  packet the data to handle
     */
    void handleMessage(uint8_t *packet);

    /**
     * @brief sends the current state of the defined sensors to the game server
     * @param type the type of the sensor
     */
    void sendSensorUpdate(SensorModule::SensorType type);

  private:
    /**
     * IP address of the game server to send messages to
     */
    uint8_t _gameServerAddress[4];

    /**
     * The UDP address of the game server to send messages to
     */
    uint16_t _gameServerPort;

    /**
     * The board instance.
     */
    Board *_board;
};

#endif
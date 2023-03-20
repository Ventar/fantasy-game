#ifndef UDP_CONNECTION_H_
#define UDP_CONNECTION_H_

#include <Arduino.h>
#include <ESPmDNS.h>
#include <WiFi.h>
#include <WiFiUdp.h>

#define UDP_CALLBACK [](UDPConnection * udp, uint8_t * data)
#define UDP_MESSAGE_REGISTER 0
#define UDP_MESSAGE_CLEAR_COLORS 1

class UDPConnection;

/**
 * Update function that is triggered when a sensor state has changed.
 */
typedef void (*MessageHandlerFunction)(UDPConnection *udp, uint8_t *data);

/**
 * @brief service that establishes a connection to the server via UDP. Uses the MDNS service to enable discovery between the device and the server. Every device
 * has exactly one server (the main game server) to which it is connected.
 */
class UDPConnection {

  public:
    /**
     * @brief creates a service instance and starts the MDNS service.
     *
     * @param   mdnsName the MDNS service name that is used by this device
     * @param   udpPort  the UDP port that is used by this device
     */
    UDPConnection(const char *mdnsName, uint16_t udpPort);

    /**
     * @brief logic to handle incoming packets
     * @param  packet the data to handle
     */
    virtual void handleMessage(uint8_t *packet) = 0;

    /**
     * Checks if a new UDP package was received and handles the content.
     */
    void update();

  protected:
    /**
     *  the UDP connection to receive and send data from and to the server.
     */
    WiFiUDP *_udp;

    /**
     * Array to hold an incoming message from the server
     */
    uint8_t incomingPacket[512];

    /**
     * The MDNS service name that is used by this device
     */
    const char *_mdnsName;

    /**
     * the UDP port that is used by this device
     */
    uint16_t _udpPort;
};

#endif
#ifndef UDP_SERVICE_H_
#define UDP_SERVICE_H_

#include <Arduino.h>
#include <WiFi.h>
#include <ESPmDNS.h>
#include <WiFiUdp.h>

#define UDP_CALLBACK [](UDPService * udp, uint8_t * data)
#define UDP_MESSAGE_REGISTER 0

class UDPService;

/**
 * Update function that is triggered when a sensor state has changed.
 */
typedef void (*MessageHandlerFunction)(UDPService *udp, uint8_t *data);

/**
 * @brief service that establishes a connection to the server via UDP. Uses the MDNS service to enable discovery between the device and the server. Every device
 * has exactly one server (the main game server) to which it is connected.
 */
class UDPService {

  public:
    /**
     * @brief creates a service instance and starts the MDNS service.
     *
     * @param   mdnsName the MDNS service name that is used by this device
     * @param   udpPort  the UDP port that is used by this device
     */
    UDPService(const char *mdnsName, uint16_t udpPort);

    /**
     * @brief Registers the passed funtion to be executed upon reception of an incoming message for the given type
     * @param  eventType the event type (byte 8 in the message specification)
     * @param function the code to execute when the message was received
     */
    void on(uint8_t eventType, MessageHandlerFunction function);

    /**
     * Checks if a new UDP package was received and handles the content.
     */
    void handleUDP();

  private:
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

    /**
     * Reserve space for the first ten event types to register message handler.
     */
    MessageHandlerFunction _handler[10];
};

#endif
#ifndef ARCADE_UDP_CONNECTION_H
#define ARCADE_UDP_CONNECTION_H

#include <WiFiUdp.h>
#include <wifi/wifi_setup.h>

/**
 * The UDP server to receive messages from the neoboard game server
 */
WiFiUDP Udp;

/**
 * The name of the MDNS service
 */
const char *NB_MDNS_SERVICE = "pcontroller";
/**
 * The UDP port to start the UDP server on
 */
const uint16_t NB_UDP_PORT = 5000;

const uint16_t PACKET_SIZE = 256;

/**
 * The size of incoming UDP packets
 */
uint8_t incomingPacket[PACKET_SIZE];

/**
 * Sleep while no game is in progress
 */
int sleepMS = 500;

void handleUDP() {

  int packetSize = Udp.parsePacket();

  if (packetSize) {
    Serial.printf("Received %d bytes from %s, port %d\n", packetSize,
                  Udp.remoteIP().toString().c_str(), Udp.remotePort());
    int len = Udp.read(incomingPacket, PACKET_SIZE);

    if (len > 0) {
      incomingPacket[len] = '\0';
    }

    switch (incomingPacket[0]) {
    case 0: {
      Serial.println("command: CLEAR LED STRIP");
      sleepMS = 500;
      break;
    }
    }
  }
}

/**
 * Setup the WiFi Module for the communication
 */
void setupUDP() {

  if (!MDNS.begin(NB_MDNS_SERVICE)) {
    Serial.println("Error setting up MDNS responder!");
  }

  Serial.print("\nSetup MDNS module name ::= [" + String(NB_MDNS_SERVICE) +
               "], service ::= [" + NB_MDNS_SERVICE + "]");

  MDNS.addService(NB_MDNS_SERVICE, "udp", NB_UDP_PORT);

  Udp.begin(NB_UDP_PORT);
  Serial.printf("\nSetup UDP server on port ::= [%d]\n", NB_UDP_PORT);
}

#endif
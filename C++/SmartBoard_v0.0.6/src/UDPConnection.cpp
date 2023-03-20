
#include <UDPConnection.h>

UDPConnection::UDPConnection(const char *mdnsName, uint16_t udpPort) : _mdnsName(mdnsName), _udpPort(udpPort) {

    // Setup MDNS for automatic discovery of the board.
    // ----------------------------------------------------------------------------

    String name = WiFi.macAddress();
    name.replace(":", "");

    if (!MDNS.begin(name.c_str())) {
        Serial.println("Error setting up MDNS responder!");
    }

    Serial.print("\nSetup MDNS module name ::= [" + name + "], service ::= [" + mdnsName + "]");

    MDNS.addService(mdnsName, "udp", udpPort);

    // Setup the UDP server
    // ----------------------------------------------------------------------------

    _udp = new WiFiUDP();
    uint8_t uStart = _udp->begin(udpPort);

    if (uStart) {
        Serial.printf("\nSetup UDP server on port ::= [%d]\n", udpPort);
    } else {
        Serial.println("Could not start UDP server...");
    }
};

void UDPConnection::update() {

     //MDNS.update();

    uint16_t packetSize = _udp->parsePacket();

    if (packetSize) {

        _udp->read(incomingPacket, packetSize); // store the data in the buffer shared buffer for the connection

        Serial.printf("Received %d bytes from %s with message type ::= [%d]\n", packetSize, _udp->remoteIP().toString().c_str(), incomingPacket[0]);

        handleMessage(incomingPacket);
    }
};


#include <UDPService.h>

UDPService::UDPService(const char *mdnsName, uint16_t udpPort) : _mdnsName(mdnsName), _udpPort(udpPort) {

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

void UDPService::handleUDP() {

    //MDNS.update();

    uint16_t packetSize = _udp->parsePacket();

    if (packetSize) {

        Serial.printf("Received %d bytes from %s\n", packetSize, _udp->remoteIP().toString().c_str());
        Serial.println("");

        uint8_t packet[packetSize];

        _udp->read(packet, packetSize);

        
        Serial.printf("Received message of type ::= [%d]\n", packet[0]);

        // handle the message if a corresponding handler was defined...otherwise endup in memory areas that will destroy the programm
        _handler[packet[0]](this, packet);
    }
};

void UDPService::on(uint8_t eventType, MessageHandlerFunction function) { _handler[eventType] = function; };
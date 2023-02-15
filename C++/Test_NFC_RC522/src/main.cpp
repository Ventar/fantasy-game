// Basic MFRC522 RFID Reader Code by cooper @ my.makesmart.net
// Released under Creative Commons - CC by cooper@my.makesmart.net

#include <Arduino.h>
#include <RFIDReader.h>
#include <SPI.h>
// #include <udp_connection.h>

#define CLK 18
#define MISO 19
#define MOSI 23

#define RFID_RST 27
#define RFID_CS 25

SPIClass* vspi;
RFIDReader* reader;

void setup() {
    Serial.begin(115200);

    vspi = new SPIClass(VSPI);
    vspi->begin(CLK, MISO, MOSI);
    vspi->setFrequency(80000000);

    reader = new RFIDReader(vspi, RFID_CS, RFID_RST);

    Serial.println("RFID-Chip auflegen, um UID anzuzeigen...");
}

void loop() {
    // MDNS.update();

    if (!reader->detectCard()) {
        delay(100);
        return;
    } else {
        reader->contentToSerial();
        byte gameId[7];

        /*
                String("BG0002").getBytes(gameId, 7);
                reader->writeGameID(gameId);
                */

        reader->readGameID(gameId);
        Serial.println((char*)gameId);

        reader->deactivate();
        Serial.println("\n------------------------------------------------------------\n\n");
    }

    delay(2000);
}
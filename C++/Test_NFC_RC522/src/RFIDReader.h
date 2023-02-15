#ifndef RFID_DEVICE_h
#define RFID_DEVICE_h

#include <Arduino.h>
#include <MFRC522.h>

/**
 * Utility service to create valid NDEF content in the game space, i.e. only
 * game realted PICCS are managed here and no general NDEF records.
 *
 * @see https://ndefparser.online/
 */
class RFIDReader {
   public:
    /*
     * The RFID card reader used by this service.
     */
    MFRC522* mfrc522;
    SPISettings* spiSettings;
    MFRC522_SPI* spiDevice;

    /**
     * Creates a new RFID service from the passed reader.
     */
    RFIDReader(SPIClass* spiBus, byte cs, byte rst) {
        spiSettings = new SPISettings(SPI_CLOCK_DIV2, MSBFIRST, SPI_MODE0);
        spiDevice = new MFRC522_SPI(cs, rst, spiBus, *spiSettings);
        mfrc522 = new MFRC522(spiDevice);
        init();
    }

    /**
     * Initalizes the PCD. The SPI has to be started before.
     */
    void init() {
        // MFRC522 initialisieren
        mfrc522->PCD_Init();
//        mfrc522->PCD_SetAntennaGain(mfrc522->RxGain_38dB);

        // Details vom MFRC522 RFID READER / WRITER ausgeben
        mfrc522->PCD_DumpVersionToSerial();
    }

    /**
     * Prints the current content of the PICC to the serial interface. The PICC has to be in state active for this operation.
     */
    void contentToSerial() {
        MFRC522::StatusCode status;
        byte byteCount;
        byte buffer[18];
        byte i;

        Serial.println(F("Page  0  1  2  3"));
        // Try the mpages of the original Ultralight. Ultralight C has more pages.
        for (byte page = 0; page < 16; page += 4) {  // Read returns data for 4 pages at a time.
            // Read pages
            byteCount = sizeof(buffer);
            status = mfrc522->MIFARE_Read(page, buffer, &byteCount);
            if (status != MFRC522::STATUS_OK) {
                Serial.print(F("MIFARE_Read() failed: "));
                Serial.println(MFRC522::GetStatusCodeName(status));
                break;
            }
            // Dump data
            for (byte offset = 0; offset < 4; offset++) {
                i = page + offset;
                if (i < 10)
                    Serial.print(F("  "));  // Pad with spaces
                else
                    Serial.print(F(" "));  // Pad with spaces
                Serial.print(i);
                Serial.print(F("  "));
                for (byte index = 0; index < 4; index++) {
                    i = 4 * offset + index;
                    if (buffer[i] < 0x10)
                        Serial.print(F(" 0"));
                    else
                        Serial.print(F(" "));
                    Serial.print(buffer[i], HEX);
                }
                Serial.println();
            }
        }
    }  // End P

    /**
     * Clears all record for the current PICC.
     */
    void format() {
        byte buf[4] = {0x00, 0x00, 0x00, 0x00};

        for (byte i = 4; i < 16; i++) {
            MFRC522::StatusCode status = mfrc522->MIFARE_Ultralight_Write(i, buf, 4);

            if (status != MFRC522::STATUS_OK) {
                Serial.print(F("Format page "));
                Serial.print(i);
                Serial.print(F(" :"));
                Serial.println(mfrc522->GetStatusCodeName(status));
                return;
            }
        }
    }

    /**
     * Reads the game ID.
     */
    void readGameID(byte* gameId) {
        MFRC522::StatusCode status;
        byte len = 18;
        byte buffer[len];

        mfrc522->MIFARE_Read(4, buffer, &len);

        if (status != MFRC522::STATUS_OK) {
            Serial.print(F("Reading failed: "));
            Serial.println(mfrc522->GetStatusCodeName(status));
            return;
        }

        for (int i = 0; i < 6; ++i) gameId[i] = buffer[i];
    }

    /**
     * Clears all data on the PICC and write the game id to it. A game ID always
     * has two upper case letters for the extension and 4 digits for the unique
     * number. Example: BG0000 for the first card of the base game.
     *
     * @param gameId the game ID.
     */
    void writeGameID(byte gameId[]) {
        byte page4[4] = {
            gameId[0],  // game editon character one
            gameId[1],  // game editon character two
            gameId[2],  // digit 1
            gameId[3],  // digit 2
        };

        byte page5[4] = {
            gameId[4],  // digit 3
            gameId[5],  // digit 4
            0x00,
            0x00,
        };

        mfrc522->MIFARE_Ultralight_Write(4, page4, 4);
        mfrc522->MIFARE_Ultralight_Write(5, page5, 4);
    }

    /**
     * Checks if a new PICC was presented and if the data can be read. If this method returns true the PICC is in an active state for further operations.
     */
    bool detectCard() {
        if (!mfrc522->PICC_IsNewCardPresent()) {
            return false;
        }

        if (!mfrc522->PICC_ReadCardSerial()) {
            return false;
        }

        dump_byte_array(mfrc522->uid.uidByte, 10);
        Serial.println(" put into state active");
        return true;
    }

    void activate() {
        byte bufferATQA[2];
        byte bufferSize = sizeof(bufferATQA);

        MFRC522::StatusCode status = mfrc522->PICC_WakeupA(bufferATQA, &bufferSize);
        Serial.print(F("Wakeup: "));
        Serial.println(mfrc522->GetStatusCodeName(status));

        if (status != MFRC522::STATUS_OK) {
            return;
        }

        status = mfrc522->PICC_Select(&mfrc522->uid);
        Serial.print(F("Select: "));
        Serial.println(mfrc522->GetStatusCodeName(status));

        if (status != MFRC522::STATUS_OK) {
            return;
        }
    }

    void deactivate() {
        MFRC522::StatusCode status = mfrc522->PICC_HaltA();
        Serial.print(F("HaltA: "));
        Serial.println(mfrc522->GetStatusCodeName(status));
    }

   private:
    /**
     * Helper routine to dump a byte array as hex values to Serial.
     */
    void dump_byte_array(byte* buffer, byte bufferSize) {
        for (byte i = 0; i < bufferSize; i++) {
            Serial.print(buffer[i] < 0x10 ? " 0" : " ");
            Serial.print(buffer[i], HEX);
        }
    }
};

#endif
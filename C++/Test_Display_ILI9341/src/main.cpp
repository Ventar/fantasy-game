/***************************************************
  This is our GFX example for the Adafruit ILI9341 Breakout and Shield
  ----> http://www.adafruit.com/products/1651

  Check out the links above for our tutorials and wiring diagrams
  These displays use SPI to communicate, 4 or 5 pins are required to
  interface (RST is optional)
  Adafruit invests time and resources providing this open source code,
  please support Adafruit and open-source hardware by purchasing
  products from Adafruit!

  Written by Limor Fried/Ladyada for Adafruit Industries.
  MIT license, all text above must be included in any redistribution
 ****************************************************/



#include "Adafruit_GFX.h"
#include "Adafruit_ILI9341.h"
#include <Fonts/FreeSans12pt7b.h>
#include "RFIDReader.h"
#include "SPI.h"

// For the Adafruit shield, these are the default.
#define TFT_CS 16
#define TFT_RST 17
#define TFT_DC 21

#define TOUCH_CS 22

#define CLK 18
#define MISO 19
#define MOSI 23

#define RFID_RST 27
#define RFID_CS 25

SPIClass* vspi;
RFIDReader* reader;
Adafruit_ILI9341* tft;

// XPT2046_Touchscreen ts(TOUCH_CS);

void setup() {
    Serial.begin(115200);

    vspi = new SPIClass(VSPI);
    vspi->begin(CLK, MISO, MOSI);
    vspi->setFrequency(40000000);

    reader = new RFIDReader(vspi, RFID_CS, RFID_RST);

    tft = new Adafruit_ILI9341(vspi, TFT_DC, TFT_CS, TFT_RST);
    tft->begin(40000000);
    tft->setRotation(1);
    tft->fillScreen(ILI9341_BLACK);

    // ts.begin();
    // ts.setRotation(1);

    /*
        if (!SPIFFS.begin()) {
            Serial.println("An Error has occurred while mounting SPIFFS");
            return;
        }
        */

    // tft.drawRGBBitmap(0, 0, image_data_Skills, 320, 240);

    /*
        File f;

        f = SPIFFS.open("/treasure.raw", "r");

        if (f) {
            int s = f.size();
            Serial.printf("File Opened , Size=%d\r\n", s);
        }

        uint8_t* dynamicArray = new uint8_t[f.size()];
        if (dynamicArray == nullptr) {
            Serial.println("Could not initialize array");
        }
        // do stuff with your array

        for (int i = 0; i < f.size(); i++) {
            dynamicArray[i] = f.read();
        }

        // Serial.println(data);

        f.close();
    */

    // tft.drawRGBBitmap(0, 0, granite, 320, 240);
    //  tft.drawBitmap(90, 10, dynamicArray, 127, 127, ILI9341_LIGHTGREY);

    // tft.writePixels(granite,320*240);

    // delete[] dynamicArray;  // delete when not in use anymore
}

boolean wastouched = true;

void loop() {
    if (reader->detectCard()) {
        reader->contentToSerial();
        byte gameId[7];

        reader->readGameID(gameId);
        Serial.println((char*)gameId);

        reader->deactivate();

        tft->fillScreen(ILI9341_BLACK);
        tft->setCursor(10, 32);
        tft->setTextColor(ILI9341_WHITE);
        tft->setFont(&FreeSans12pt7b);
        tft->println((char*)gameId);
        

        // -------------
        /* File f;

        f = SPIFFS.open("/treasure.raw", "r");

        if (f) {
            int s = f.size();
            Serial.printf("File Opened , Size=%d\r\n", s);
        }

        uint8_t* dynamicArray = new uint8_t[f.size()];
        if (dynamicArray == nullptr) {
            Serial.println("Could not initialize array");
        }
        // do stuff with your array

        for (int i = 0; i < f.size(); i++) {
            dynamicArray[i] = f.read();
        }

        // Serial.println(data);

        f.close();

        tft.drawBitmap(90, 10, dynamicArray, 127, 127, ILI9341_LIGHTGREY);

        delete[] dynamicArray;

        // -------------

        // tft.drawBitmap(20, 20, questMap, 127, 127, ILI9341_WHITE);
        */
    } else {
        delay(100);
        return;
    }

    delay(2000);
}
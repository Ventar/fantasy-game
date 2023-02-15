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

#include "Arduino.h"

// For the Adafruit shield, these are the default.
// #define TFT_CS 16
// #define TFT_RST 17
// #define TFT_DC 21

// #define TOUCH_CS 22

// #define CLK 18
// #define MISO 19
// #define MOSI 23
// This example renders a png file that is stored in a FLASH array
// using the PNGdec library (available via library manager).

// Image files can be converted to arrays using the tool here:
// https://notisrac.github.io/FileToCArray/
// To use this tool:
//   1. Drag and drop file on "Browse..." button
//   2. Tick box "Treat as binary"
//   3. Click "Convert"
//   4. Click "Save as file" and move the header file to sketch folder
//   5. Open the sketch in IDE
//   6. Include the header file containing the array (panda.h in this example)

// Include the PNG decoder library
#include <PNGdec.h>
#include "panda.h" // Image is stored here in an 8 bit array

PNG png; // PNG decoder inatance

#define MAX_IMAGE_WDITH 240 // Adjust for your images

int16_t xpos = 0;
int16_t ypos = 0;

// Include the TFT library https://github.com/Bodmer/TFT_eSPI
#include "SPI.h"
#include <TFT_eSPI.h>              // Hardware-specific library
TFT_eSPI tft = TFT_eSPI();         // Invoke custom library

//=========================================v==========================================
//                                      pngDraw
//====================================================================================
// This next function will be called during decoding of the png file to
// render each image line to the TFT.  If you use a different TFT library
// you will need to adapt this function to suit.
// Callback function to draw pixels to the display
void pngDraw(PNGDRAW *pDraw) {
  uint16_t lineBuffer[MAX_IMAGE_WDITH];
  png.getLineAsRGB565(pDraw, lineBuffer, PNG_RGB565_BIG_ENDIAN, 0xffffffff);
  tft.pushImage(xpos, ypos + pDraw->y, pDraw->iWidth, 1, lineBuffer);
}


//====================================================================================
//                                    Setup
//====================================================================================
void setup()
{
  Serial.begin(115200);
  Serial.println("\n\n Using the PNGdec library");

  // Initialise the TFT
  tft.begin();
  tft.fillScreen(TFT_BLACK);

  Serial.println("\r\nInitialisation done.");
}

//====================================================================================
//                                    Loop
//====================================================================================
void loop()
{
  int16_t rc = png.openFLASH((uint8_t *)panda, sizeof(panda), pngDraw);
  if (rc == PNG_SUCCESS) {
    Serial.println("Successfully png file");
    Serial.printf("image specs: (%d x %d), %d bpp, pixel type: %d\n", png.getWidth(), png.getHeight(), png.getBpp(), png.getPixelType());
    tft.startWrite();
    uint32_t dt = millis();
    rc = png.decode(NULL, 0);
    Serial.print(millis() - dt); Serial.println("ms");
    tft.endWrite();
    // png.close(); // not needed for memory->memory decode
  }
  delay(3000);
  tft.fillScreen(random(0x10000));
}



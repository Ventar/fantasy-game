#include <sensor.h>

// the PIN that controls the LED strip
#define LED_IN D5

// the initial brightness of the LEDS between 0 and 100
#define LED_INIT_BRIGHTNESS 50

/**
 * @brief controller for the LED strip of the board
  */
Adafruit_NeoPixel strip(FIELD_COUNT *FIELD_COUNT, LED_IN, NEO_GRB + NEO_KHZ800);

/**
 * @brief Sets the color of the field
 * 
 * @param degree the orientation of the board
 * @param column the column
 * @param row the row
 * @param r red value
 * @param g green value
 * @param b blue value
 */
void setFieldColor(BoardRotation degree, int column, int row, int r, int g, int b)
{
    strip.setPixelColor(getField(degree, column, row)->pixelNo, r, g, b);
}

/**
 * @brief performs the setup of the neopixelx
 * 
 */
void setupNeoPixel()
{

    // Initialize the NeoPixels and fill the field matrix
    strip.begin();
    strip.setBrightness(200);
    strip.clear();
    strip.show();

    // initialize the fields with the pixel number
    for (int row = 0; row < FIELD_COUNT; row++) {
        for (int column = 0; column < FIELD_COUNT; column++) {
            int pixelNo = row * FIELD_COUNT + column;
            fields[column][row].pixelNo = pixelNo;
            strip.setPixelColor(pixelNo, 0);
            strip.show();
        }
    }
}
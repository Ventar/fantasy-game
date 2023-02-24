#ifndef CUSTOM_NP_H_
#define CUSTOM_NP_H_

#include <Adafruit_NeoPixel.h>
#include <Arduino.h>

#define C Adafruit_NeoPixel::Color

// https://www.rapidtables.com/web/color/RGB_Color.html

#define Black 0
#define SlateGrey 1
#define Gray 2
#define LightGrey 3
#define White 4
#define DarkRed 5
#define Crimson 6
#define Red 7
#define Coral 8
#define OrangeRed 9
#define Orange 10
#define Chocolate 11
#define Yellow 12
#define Gold 13
#define DarkGreen 14
#define GreenYellow 15
#define LawnGreen 16
#define Lime 17
#define LightGreen 18
#define SpringGreen 19
#define LightSeaGreen 20
#define Teal 21
#define Aqua 22
#define Turquoise 23
#define AquaMarine 24
#define PowderBlue 25
#define SteelBlue 26
#define CornFlowerBlue 27
#define DeepSkyBlue 28
#define SkyBlue 29
#define MidnightBlue 30
#define DarkBlue 31
#define MediumBlue 32
#define Blue 33
#define RoyalBlue 34
#define BlueViolet 35
#define Indigo 36
#define MediumPurple 37
#define DarkMagenta 38
#define DarkViolet 39
#define Magenta 40
#define Orchid 41
#define DeepPink 42
#define Pink 43
#define LightGoldenRodYellow 44
#define MintCream 45

static const uint32_t Color[] = {
    C(0, 0, 0),       // Black
    C(112, 128, 144), // Slate Grey
    C(128, 128, 128), // Grey
    C(211, 211, 211), // Light Grey
    C(255, 255, 255), // White
    C(139, 0, 0),     // Dark Red
    C(220, 20, 60),   // Crimson
    C(255, 0, 0),     // Red
    C(255, 127, 80),  // Red
    C(255, 69, 0),    // Orange Red
    C(255, 165, 0),   // Orange
    C(210, 105, 30),  // Chocolate
    C(255, 255, 0),   // Yellow
    C(255, 215, 0),   // Gold
    C(0, 100, 0),     // Dark Green
    C(173, 255, 47),  // Green Yellow
    C(124, 252, 0),   // Lawn Green
    C(0, 255, 0),     // Lime
    C(144, 238, 144), // Light Green
    C(0, 255, 127),   // Spring Green
    C(32, 178, 170),  // Light Sea Green
    C(0, 128, 128),   // Teal
    C(0, 255, 255),   // Aqua
    C(64, 224, 208),  // Turquoise
    C(127, 255, 212), // Aqua Marine
    C(176, 224, 230), // Powder Blue
    C(70, 130, 180),  // Steel Blue
    C(100, 149, 237), // Corn Flower Blue
    C(0, 191, 255),   // Deep Sky Blue
    C(25, 25, 112),   // Midnight Blue
    C(0, 0, 139),     // Darkt Blue
    C(0, 0, 205),     // Medium Blue
    C(0, 0, 255),     // Blue
    C(65, 105, 225),  // Royal Blue
    C(138, 43, 226),  // Blue Violet
    C(75, 0, 130),    // Indigo
    C(147, 112, 219), // Medium Purple
    C(139, 0, 139),   // Dark Magenta
    C(148, 0, 212),   // Dark Violet
    C(255, 0, 255),   // Magenta
    C(218, 112, 214), // Orchid
    C(255, 20, 127),  // Deep Pink
    C(255, 192, 203), // Pink
    C(250, 250, 210), // Light Golden Rod Yellow
    C(245, 255, 250), // Mint Cream

};

/**
 * @brief Extension to the adrafruit neopixel strip with predefined colors and some utility methods
 */
class CustomNeoPixel : public Adafruit_NeoPixel {

  public:
    /**
     * @brief creates a new strip.
     *
     * @param   n  Number of NeoPixels in strand.
     * @param   p  Arduino pin number which will drive the NeoPixel data in.
     */

    CustomNeoPixel(uint16_t n, uint16_t p);

    /**
     * Sets the color of all pixels to the passed one.
     * @param color the color
     */
    void setColor(uint32_t color);
};

#endif
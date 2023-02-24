#include <Adafruit_NeoPixel.h>
#include <Arduino.h>

#define PIXELS 16

Adafruit_NeoPixel strip(PIXELS,18, NEO_GRB + NEO_KHZ800);

void setup() {
    strip.begin();
    strip.setBrightness(64);
    strip.clear();
    strip.show();

    Serial.begin(115200);
    Serial.println("\n\nLED Test 1.0");
}

void loop() {

    strip.clear();
    strip.setPixelColor(0, strip.Color(255, 0, 0));
    strip.setPixelColor(1, strip.Color(0, 255, 0));
    strip.setPixelColor(2, strip.Color(0, 0, 255));
    strip.setPixelColor(3, strip.Color(255, 255, 0));
    /*strip.setPixelColor(4, strip.Color(0, 255, 255));
    strip.setPixelColor(5, strip.Color(255, 0, 255));
    strip.setPixelColor(6, strip.Color(255, 255, 255));

    strip.setPixelColor(7, strip.Color(128, 255, 0));
    strip.setPixelColor(8, strip.Color(0, 128, 255));
    strip.setPixelColor(9, strip.Color(128, 0, 255));
    strip.setPixelColor(10, strip.Color(128, 255, 255));

    strip.setPixelColor(11, strip.Color(255, 128, 0));
    strip.setPixelColor(12, strip.Color(0, 255, 128));
    strip.setPixelColor(13, strip.Color(255, 0, 128));
    strip.setPixelColor(14, strip.Color(128, 128, 255));
    strip.setPixelColor(15, strip.Color(255, 128, 128));
    */
    strip.show();
    delay(10000);
    strip.clear();
    strip.show();
    delay(10000);
}

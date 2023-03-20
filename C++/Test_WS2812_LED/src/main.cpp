#include <Adafruit_NeoPixel.h>
#include <Arduino.h>

#define PIXELS 64

Adafruit_NeoPixel strip(PIXELS, 22, NEO_GRB + NEO_KHZ800);

void setup() {
    strip.begin();
    strip.setBrightness(16);
    strip.clear();
    strip.show();

    Serial.begin(115200);
    Serial.println("\n\nLED Test 1.0");
}

void loop() {

    strip.clear();

    for (int i = 0; i < PIXELS; i++) {
        strip.clear();
        strip.setPixelColor(i, strip.Color(255, 0, 0));
        strip.show();
        Serial.printf("Set pixel : %d\n", i);
        delay(500);
    }
    
}

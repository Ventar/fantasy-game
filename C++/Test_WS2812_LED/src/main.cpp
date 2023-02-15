#include <Arduino.h>
#include <Adafruit_NeoPixel.h>

#define PIXELS 8

Adafruit_NeoPixel strip(PIXELS, D5, NEO_GRB + NEO_KHZ800);

void setup()
{
  strip.begin();
  strip.setBrightness(32);
  strip.clear();
  strip.show();

  Serial.begin(115200);
  Serial.println("\n\nLED Test 1.0");
}

void loop()
{

  for (int i = 0; i < PIXELS; i++)
  {
    strip.clear();
    strip.setPixelColor(i, strip.Color(255, 0, 0));
    strip.show();
    delay(500);
  }

  strip.clear();
  strip.show();

  delay(1000);
}

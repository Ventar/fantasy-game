#include <Arduino.h>
#include <Adafruit_NeoPixel.h>

Adafruit_NeoPixel strip(4, D5, NEO_GRB + NEO_KHZ800);

void setup()
{
  Serial.begin(115200);

  pinMode(D1, INPUT);
  pinMode(D2, INPUT);
  pinMode(D3, INPUT);
  pinMode(D4, INPUT);

  delay(500);

  // Initialize the NeoPixels and fill the field matrix
  strip.begin();
  strip.setBrightness(128);
  strip.clear();
  strip.show();

  strip.setPixelColor(0, 0, 0, 0);
  strip.setPixelColor(1, 0, 0, 0);
  strip.setPixelColor(2, 0, 0, 0);
  strip.setPixelColor(3, 0, 0, 0);

  strip.show();

  delay(500);

  Serial.println("\n\nStarted SMD HAL TEST...");
}

void loop()
{
  int h00 = digitalRead(D4);
  int h01 = digitalRead(D3);
  int h02 = digitalRead(D2);
  int h03 = digitalRead(D1);

  Serial.println("\n--------------------");

  strip.setPixelColor(0, 0, 0, 0);
  strip.setPixelColor(1, 0, 0, 0);
  strip.setPixelColor(2, 0, 0, 0);
  strip.setPixelColor(3, 0, 0, 0);
  strip.show();

  if (h00 == 1)
  {
    Serial.println("H00 - OFF");
  }
  else
  {
    Serial.println("H00 - ON");
    strip.setPixelColor(0, 255, 0, 0);
    strip.setPixelColor(1, 255, 0, 0);
    strip.setPixelColor(2, 255, 0, 0);
    strip.setPixelColor(3, 255, 0, 0);
    strip.show();
  }

  if (h01 == 1)
  {
    Serial.println("H01 - OFF");
  }
  else
  {
    Serial.println("H01 - ON");
    strip.setPixelColor(0, 255, 255, 0);
    strip.setPixelColor(1, 255, 255, 0);
    strip.setPixelColor(2, 255, 255, 0);
    strip.setPixelColor(3, 255, 255, 0);
    strip.show();
  }

  if (h02 == 1)
  {
    Serial.println("H02 - OFF");
  }
  else
  {
    Serial.println("H02 - ON");
    strip.setPixelColor(0, 0, 255, 0);
    strip.setPixelColor(1, 0, 255, 0);
    strip.setPixelColor(2, 0, 255, 0);
    strip.setPixelColor(3, 0, 255, 0);
    strip.show();
  }

  if (h03 == 1)
  {
    Serial.println("H03 - OFF");
  }
  else
  {
    Serial.println("H03 - ON");
    strip.setPixelColor(0, 0, 0, 255);
    strip.setPixelColor(1, 0, 0, 255);
    strip.setPixelColor(2, 0, 0, 255);
    strip.setPixelColor(3, 0, 0, 255);
    strip.show();
  }

  delay(1000);
}
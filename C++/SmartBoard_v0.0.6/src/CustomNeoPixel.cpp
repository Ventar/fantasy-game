#include <CustomNeoPixel.h>

CustomNeoPixel::CustomNeoPixel(uint16_t n, uint16_t p) : Adafruit_NeoPixel(n, p, NEO_GRB + NEO_KHZ800) {
    begin();
    setBrightness(220);
    clear();
    show();
}

void CustomNeoPixel::setColor(uint32_t color) {

    for (byte px = 0; px < numPixels(); px++) {
        setPixelColor(px, color);
    }
    show();
}
#include <Adafruit_NeoPixel.h>
#include <Arduino.h>
#include <PCA9555.h>
#include <Wire.h>

#define BUTTON_I2C_ADDRESS 0x20
#define SENSOR_I2C_ADDRESS 0x21
#define TCA_I2C_ADDRESS 0x70

/**
 * Color definition struct.
 */
struct Color {
  int red = 0;
  int green = 0;
  int blue = 0;
};

Color BLACK = {0, 0, 0};
Color WHITE = {255, 255, 255};
Color RED = {255, 0, 0};
Color LIGHT_RED = {255, 138, 128};
Color DARK_RED = {183, 28, 28};
Color GREEN = {0, 255, 0};
Color LIGHT_GREEN = {185, 246, 202};
Color DARK_GREEN = {51, 105, 30};
Color BLUE = {0, 0, 255};
Color LIGHT_BLUE = {128, 222, 234};
Color DARK_BLUE = {13, 71, 161};
Color VIOLET = {81, 45, 168};
Color LIGHT_VIOLET = {225, 190, 231};
Color DARK_VIOLET = {94, 53, 177};
Color YELLOW = {255, 192, 0};
Color MINT = {100, 255, 218};
Color ORANGE = {255, 109, 0};

/**
 * A module that consists of a HAL board that contains the sensors aggregated by
 * an PCA9555, a LED board and a board with buttons again aggregated by an
 * PCA9555. The LED board is handled seperately by concatenating all boards via
 * DIN and DOUT, but the PCA9555 board of a module are connected to a TCA9548
 * IC2 mutliplexer to use multiple of them.
 */
struct Module {
  String name;      // Name, just to differentiate on the serial out
  int channel = 0;  // The TCA9548 channel to which the module is connected
  PCA9555 mxBtn;    // Button Controller
  PCA9555 mxSensor; // HAL Sensor Controller
};

Module modA = {"Module A", 0, PCA9555(BUTTON_I2C_ADDRESS),
               PCA9555(SENSOR_I2C_ADDRESS)};

Module modB = {"Module B", 1, PCA9555(BUTTON_I2C_ADDRESS),
               PCA9555(SENSOR_I2C_ADDRESS)};

Adafruit_NeoPixel strip(4, D5, NEO_GRB + NEO_KHZ800); // NeoPixel LED strip
bool fieldEnabled[4] = {1, 1, 1, 1}; // if a field is enabled or disabled
int counter = 0;

/**
 * Prints the current state of the passed module to serial out.
 *
 * @param mod the module to print information for
 */
void printState(Module *mod);

/**
 * Initializes one of the board multiplexers. All multiplexers run in INPUT mode
 * only and provide various information, from HAL sensor states, to pressed
 * buttons and interrupts.
 *
 * @param module the module to initialize
 */
void initializeModule(Module *mod);

/**
 * Method to set a pixel color.
 *
 * @param pixel the pixel to set
 * @param color the color
 */
void color(int pixel, Color color);

/**
 * Method to set a pixel color.
 *
 * @param pixel the pixel to set
 * @param color0 the color of field 0
 * @param color1 the color of field 1
 * @param color2 the color of field 2
 * @param color3 the color of field 3
 */
void color(Color color0, Color color1, Color color2, Color color3);

/**
 * Checks if a button was pressed and performs the necessary actions.
 */
void checkButton();

/**
 * Sets the channel of the I2C mutliplexer to control the individual modules.
 */
void setTCAChannel(byte i);

/**
 * Changes the colors of the LEDs based on the sensor and button states
 */
void updateLEDs();

void setup() {
  delay(200);
  strip.setBrightness(64);
  strip.begin();
  strip.clear();
  strip.setPixelColor(0, 0);
  strip.show();

  Serial.begin(115200);
  Serial.println("\n\nSmart Board 0.0.6\n");

  initializeModule(&modA);

  pinMode(D6, INPUT); // interrupt for mxSensor
  pinMode(D7, INPUT); // interrupt for mxBtn

  strip.setPixelColor(0, 0);
  strip.show();

  Serial.println("\n\nStarted Smart Board...");
}

void loop() {

  printState(&modA);
  printState(&modB);
  delay(2000);
}

void printState(Module *mod) {

  setTCAChannel(mod->channel);
  mod->mxSensor.pinStates();
  mod->mxBtn.pinStates();

  Serial.print("---- (");
  Serial.print(counter++);
  Serial.print(") ");
  Serial.print(mod->name);
  Serial.println(" ----");

  Serial.print("button : ");
  for (uint8_t i = 0; i < 4; i++) {
    Serial.print(mod->mxBtn.stateOfPin(i));
    Serial.print(" ");
  }
  Serial.println("");

  Serial.print("sensor : ");
  for (uint8_t i = 0; i < 16; i++) {
    Serial.print(mod->mxSensor.stateOfPin(i));
    Serial.print(" ");
  }
  Serial.println("");

  Serial.println("\n");
}

void checkButton() {
  /*
    mxBtn.pinStates(); // read the complete register.
    strip.clear();

    for (uint8_t i = 0; i < 4; i++)
    {

      if (mxBtn.stateOfPin(i) == 1)
      {
        fieldEnabled[i] = !fieldEnabled[i]; // switch the state of the field

        if (fieldEnabled[i])
          color(i, GREEN);
        else
          color(i, RED);

        strip.show();
      }

      // Display the button LED as RED unless the Button is released
      while (mxBtn.digitalRead(i) == 1)
      {
        delay(250);
      }

      color(i, BLACK);
      strip.show();
    }
    */
}

void updateLEDs() {
  /*
  mx.pinStates(); // read the complete register.
  strip.clear();

  for (uint8_t i = 0; i < 16; i++)
  {

    if (mx.stateOfPin(i) == 0 && fieldEnabled[i / 4] == 1)
    {

      switch (i)
      {
        // --- LED 0 -------------
      case 0:
        color(YELLOW, BLACK, DARK_GREEN, BLACK);
        break;
      case 1:
        color(YELLOW, DARK_GREEN, BLACK, BLACK);
        break;
      case 2:
        color(YELLOW, BLACK, DARK_RED, BLACK);
        break;
      case 3:
        color(YELLOW, DARK_RED, BLACK, BLACK);
        break;
        // --- LED 1 -------------
      case 4:
        color(BLACK, YELLOW, BLACK, DARK_GREEN);
        break;
      case 5:
        color(DARK_RED, YELLOW, BLACK, BLACK);
        break;
      case 6:
        color(BLACK, YELLOW, BLACK, DARK_RED);
        break;
      case 7:
        color(DARK_GREEN, YELLOW, BLACK, BLACK);
        break;
        // --- LED 2 -------------
      case 8:
        color(DARK_RED, BLACK, YELLOW, BLACK);
        break;
      case 9:
        color(BLACK, BLACK, YELLOW, DARK_GREEN);
        break;
      case 10:
        color(DARK_GREEN, BLACK, YELLOW, BLACK);
        break;
      case 11:
        color(BLACK, BLACK, YELLOW, DARK_RED);
        break;
        // --- LED 3 -------------
      case 12:
        color(BLACK, DARK_RED, BLACK, YELLOW);
        break;
      case 13:
        color(BLACK, BLACK, DARK_RED, YELLOW);
        break;
      case 14:
        color(BLACK, DARK_GREEN, BLACK, YELLOW);
        break;
      case 15:
        color(BLACK, BLACK, DARK_GREEN, YELLOW);
        break;

      default:
        break;
      }
    }
  }

  strip.show();
  */
}

void color(int pixel, Color color) {
  strip.setPixelColor(pixel, strip.Color(color.red, color.green, color.blue));
}

void color(Color color0, Color color1, Color color2, Color color3) {
  strip.setPixelColor(0, strip.Color(color0.red, color0.green, color0.blue));
  strip.setPixelColor(1, strip.Color(color1.red, color1.green, color1.blue));
  strip.setPixelColor(2, strip.Color(color2.red, color2.green, color2.blue));
  strip.setPixelColor(3, strip.Color(color3.red, color3.green, color3.blue));
}

void initializeModule(Module *mod) {
  setTCAChannel(mod->channel);
  for (int i = 0; i < 16; i++) {
    mod->mxBtn.pinMode(i, INPUT);
    mod->mxSensor.pinMode(i, INPUT);
  }
}

void setTCAChannel(byte i) {
  Wire.beginTransmission(TCA_I2C_ADDRESS);
  Wire.write(1 << i);
  Wire.endTransmission();
}

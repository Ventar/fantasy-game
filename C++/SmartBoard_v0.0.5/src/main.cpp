#include <Arduino.h>
#include <PCA9555.h>
#include <Adafruit_NeoPixel.h>

/**
 * Color definition struct.
 */
struct Color
{
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

PCA9555 mxBtn(0x20);                                  // Button Controller
PCA9555 mx(0x21);                               // HAL Sensor Controller
Adafruit_NeoPixel strip(4, D5, NEO_GRB + NEO_KHZ800); // NeoPixel LED strip
bool fieldEnabled[4] = {1, 1, 1, 1};                  // if a field is enabled or disabled
int counter = 0;

/**
 * Prints the current state of sensors, fields and buttons including interrupts to
 * serial out.
 */
void printState();

/**
 * Initializes one of the board multiplexers. All multiplexers run in INPUT mode only and provide
 * various information, from HAL sensor states, to pressed buttons and interrupts.
 *
 * @param mx the multiplexer to initialize
 */
void initializeMultiPlexer(PCA9555 *mx);

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
 * Changes the colors of the LEDs based on the sensor and button states
 */
void updateLEDs();

void setup()
{
  delay(200);
  strip.setBrightness(64);
  strip.begin();
  strip.clear();
  strip.setPixelColor(0, 0);
  strip.show();

  Serial.begin(115200);
  Serial.println("\n\nSmart Board 1.0\n");

  Serial.println("Initialize PCA9555 multiplexer...");
  initializeMultiPlexer(&mxBtn);
  initializeMultiPlexer(&mx);

  pinMode(D6, INPUT); // interrupt for mxSensor
  pinMode(D7, INPUT); // interrupt for mxBtn

  strip.setPixelColor(0, 0);
  strip.show();

  Serial.println("\n\nStarted HAL Board...");
}

void loop()
{

  // strip.setPixelColor(0, 0);
  // strip.show();

  if (digitalRead(D7) == 0)
  {
    checkButton();
    updateLEDs();
    printState();
  }

  if (digitalRead(D6) == 0)
  {
    updateLEDs();
    printState();
  }

  delay(200);
}

void printState()
{

  Serial.print("---- ");
  Serial.print(counter++);
  Serial.println(" ----");

  Serial.print("Interrupt button: ");
  Serial.println(digitalRead(D7));
  Serial.print("Interrupt sensor: ");
  Serial.println(digitalRead(D6));

  Serial.print("button : ");
  for (uint8_t i = 0; i < 4; i++)
  {
    Serial.print(mxBtn.stateOfPin(i));
    Serial.print(" ");
  }
  Serial.println("");

  Serial.print("field enabled : ");
  for (uint8_t i = 0; i < 4; i++)
  {
    Serial.print(fieldEnabled[i]);
    Serial.print(" ");
  }
  Serial.println("");

  Serial.print("sensor : ");
  for (uint8_t i = 0; i < 16; i++)
  {
    Serial.print(mx.stateOfPin(i));
    Serial.print(" ");
  }
  Serial.println("");

  Serial.println("\n");
}

void checkButton()
{

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
}

void updateLEDs()
{
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
}

void color(int pixel, Color color)
{
  strip.setPixelColor(pixel, strip.Color(color.red, color.green, color.blue));
}

void color(Color color0, Color color1, Color color2, Color color3)
{
  strip.setPixelColor(0, strip.Color(color0.red, color0.green, color0.blue));
  strip.setPixelColor(1, strip.Color(color1.red, color1.green, color1.blue));
  strip.setPixelColor(2, strip.Color(color2.red, color2.green, color2.blue));
  strip.setPixelColor(3, strip.Color(color3.red, color3.green, color3.blue));
}

void initializeMultiPlexer(PCA9555 *mx)
{
  mx->begin();
  mx->setClock(100000);

  for (int i = 0; i < 16; i++)
  {
    mx->pinMode(i, INPUT);
  }
}

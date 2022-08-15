#include <Arduino.h>
#include <Adafruit_NeoPixel.h>
#include <ESP8266WiFi.h>
#include <ESP8266mDNS.h>
#include <WiFiUdp.h>

// How many NeoPixels are attached to the Arduino?
#define LED_COUNT 36

const char *NB_WIFI_SSID = "HouseOfTeens";
const char *NB_WIFI_PASSWORD = "8882941015907883";

//const char *NB_WIFI_SSID = "MRO Finger Weg";
//const char *NB_WIFI_PASSWORD = "7b559b98adc2";

/**
 * The neopixel strip that is controlled
 */
Adafruit_NeoPixel strip = Adafruit_NeoPixel(LED_COUNT, D6, NEO_GRB + NEO_KHZ800);
const uint8_t INITIAL_BRIGHTNESS = 10;

/**
 * The UDP server to receive messages from the neoboard game server
 */
WiFiUDP Udp;
const char *NB_MDNS_SERVICE = "neoboard";
const uint16_t NB_UDP_PORT = 4000;
unsigned char incomingPacket[512];

/**
 * Definition of a module.
 */
struct ModuleInitialization
{
  String name;
  uint32_t cq1;
  uint32_t cq2;
  uint32_t cq3;
  uint32_t cq4;
};

ModuleInitialization m1{"M1", Adafruit_NeoPixel::Color(255, 0, 0), Adafruit_NeoPixel::Color(255, 255, 0), Adafruit_NeoPixel::Color(255, 0, 255), Adafruit_NeoPixel::Color(0, 255, 255)};
ModuleInitialization m2{"M2", Adafruit_NeoPixel::Color(255, 255, 0), Adafruit_NeoPixel::Color(0, 0, 255), Adafruit_NeoPixel::Color(0, 255, 64), Adafruit_NeoPixel::Color(255, 0, 255)};

ModuleInitialization moduleInit = m1;

/**
 * Setup the WiFi Module for the communication
 */
void setupConnectivity()
{

  // Setup WiFI
  // ----------------------------------------------------------------------------

  //String hostname = "Module_" + moduleInit.name; 
  //WiFi.setHostname(hostname.c_str());
  WiFi.begin(NB_WIFI_SSID, NB_WIFI_PASSWORD);

  Serial.print("\n\nConnecting...");

  while (WiFi.status() != WL_CONNECTED)
  {
    delay(1000);
    Serial.print(".");
  }

  Serial.printf("\nConnected ip ::= [%s]...", WiFi.localIP().toString().c_str());
  Serial.printf("\nConnected MAC ::= [%s]...", WiFi.macAddress().c_str());

  // Setup MDNS for automatic discovery of the board.
  // ----------------------------------------------------------------------------

  if (!MDNS.begin(moduleInit.name))
  {
    Serial.println("Error setting up MDNS responder!");
  }

  Serial.printf("\nSetup MDNS module ::= [%s], service ::= [%s]", moduleInit.name, NB_MDNS_SERVICE);

  MDNS.addService(NB_MDNS_SERVICE, "udp", NB_UDP_PORT);

  // Setup the UDP server
  // ----------------------------------------------------------------------------
  Udp.begin(NB_UDP_PORT);
  Serial.printf("\nSetup UDP server on port ::= [%d]\n", NB_UDP_PORT);
  Serial.println("Started...");
}

/**
 * Setup the neopixel strip.
 */
void setupNeoPixel()
{

  strip.setPixelColor(0, moduleInit.cq1);
  strip.setPixelColor(1, moduleInit.cq1);
  strip.setPixelColor(2, moduleInit.cq1);
  strip.setPixelColor(10, moduleInit.cq1);
  strip.setPixelColor(11, moduleInit.cq1);
  strip.setPixelColor(12, moduleInit.cq1);

  strip.setPixelColor(3, moduleInit.cq2);
  strip.setPixelColor(4, moduleInit.cq2);
  strip.setPixelColor(5, moduleInit.cq2);
  strip.setPixelColor(6, moduleInit.cq2);
  strip.setPixelColor(7, moduleInit.cq2);
  strip.setPixelColor(17, moduleInit.cq2);

  strip.setPixelColor(18, moduleInit.cq3);
  strip.setPixelColor(28, moduleInit.cq3);
  strip.setPixelColor(29, moduleInit.cq3);
  strip.setPixelColor(30, moduleInit.cq3);
  strip.setPixelColor(31, moduleInit.cq3);
  strip.setPixelColor(32, moduleInit.cq3);

  strip.setPixelColor(23, moduleInit.cq4);
  strip.setPixelColor(24, moduleInit.cq4);
  strip.setPixelColor(25, moduleInit.cq4);
  strip.setPixelColor(33, moduleInit.cq4);
  strip.setPixelColor(34, moduleInit.cq4);
  strip.setPixelColor(35, moduleInit.cq4);

  strip.show(); // Initialize all pixels to 'off'
}

void setup()
{

  Serial.begin(115200);

  Serial.print("\n\n###############");
  Serial.print("\n## Neo Board ##");
  Serial.print("\n###############\n");

  strip.begin();
  strip.setBrightness(INITIAL_BRIGHTNESS);

  for (int i = 0; i < 36; i++)
  {
    strip.setPixelColor(i, 0);
  }

  strip.show();

  setupConnectivity();
}

void handleUDP()
{
  int packetSize = Udp.parsePacket();

  if (packetSize)
  {
    Serial.printf("Received %d bytes from %s, port %d\n", packetSize, Udp.remoteIP().toString().c_str(), Udp.remotePort());
    int len = Udp.read(incomingPacket, 512);

    if (len > 0)
    {
      incomingPacket[len] = '\0';
    }

    for (size_t i = 0; i < packetSize; i++)
    {
      Serial.print(incomingPacket[i]);
      Serial.println("");
    }

    switch (incomingPacket[0])
    {
    case 0:
    {

      Serial.printf("command: SETUP_PATTERN");
      setupNeoPixel();
      break;
    }
    case 1:
    {
      Serial.printf("command: RESET_PIXEL\n");
      for (int i = 0; i < strip.numPixels(); i++)
      {
        strip.setPixelColor(i, 0);
      }
      strip.show();
      break;
    }
    case 2:
    {
      Serial.printf("command: SHOW_PIXEL\n");

      for (int i = 0; i < 109; i++)
      {
        strip.setPixelColor(
            i,
            incomingPacket[1 + (i * 3) + 0],
            incomingPacket[1 + (i * 3) + 1],
            incomingPacket[1 + (i * 3) + 2]);
      }

      strip.show();

      break;
    }
    case 3:
    {
      Serial.printf("command: SET_BRIGHTNESS to %d\n", incomingPacket[1]);
      strip.setBrightness(incomingPacket[1]);
      strip.show();
    }
    }
  }
}

void loop()
{
  //float rawValue = analogRead(A0) - 840;
  //Serial.print("Reading Raw: ");
  //Serial.println(rawValue);
  //delay(500);
  MDNS.update();
  handleUDP();
}
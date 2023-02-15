
#ifndef ARCACE_WIFI_SETUP_H
#define ARCACE_WIFI_SETUP_H

#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266mDNS.h>
#include <wifi/WiFiManager.h>


/**
 * Wifi manager to collect Credetials and connect to WiFi
 */
WiFiManager wifiManager;

void resetWifi() {
wifiManager.resetSettings();
}


void connectToWifi()
{

  Serial.print("Connecting to WiFi...");

  WiFi.mode(WIFI_STA);

  //wifiManager.setConfigPortalBlocking(false);
  wifiManager.setConfigPortalTimeout(300);

  //wifiManager.setCaptivePortalEnable(false);

  wifiManager.setConnectTimeout(20);
  wifiManager.setSaveConnectTimeout(20);

  wifiManager.setDarkMode(true);
  wifiManager.setShowInfoUpdate(false);
  const char * menu[] = {"wifi","exit"};
  wifiManager.setMenu(menu,2);
  wifiManager.setMinimumSignalQuality(30);
  wifiManager.setSaveConfigCallback([](){ESP.restart();});
  
  
  

  bool res = wifiManager.autoConnect("War of Elements");

  if (res)
  {
    Serial.println("");
    Serial.println("WiFi connected");
    Serial.println("IP address: ");
    Serial.println(WiFi.localIP());
  }
  else
  {
    Serial.println("");
    Serial.println("CANNOT connect to WiFi");
  }
  
}

#endif
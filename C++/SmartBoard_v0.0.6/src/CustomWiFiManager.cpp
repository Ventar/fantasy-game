#include <CustomWiFiManager.h>

CustomWiFiManager::CustomWiFiManager(char const *apName, std::function<void(WiFiManager *)> configCallback,
                                     std::function<void(WiFiManager *)> connectedCallback)
    : WiFiManager() {

    _connectedCallback = connectedCallback;
    _apName = apName;

    WiFi.mode(WIFI_STA);
    const char *menu[] = {"wifi", "exit"};

    // wifiManager.setConfigPortalBlocking(false);
    // wifiManager.setConfigPortalTimeout(300);
    // wifiManager.setCaptivePortalEnable(false);
    setConnectTimeout(20);
    setSaveConnectTimeout(20);
    setDarkMode(true);
    setShowInfoUpdate(false);
    setMenu(menu, 2);
    setMinimumSignalQuality(30);
    setCleanConnect(true);
    // wifiManager.setSaveConfigCallback([]() { ESP.restart(); });
    setAPCallback(configCallback);
    connect();

};

void CustomWiFiManager::connect() {
    bool res = autoConnect(_apName);

    if (res) {

        Serial.println("");
        Serial.println("WiFi connected");
        Serial.println("IP address: ");
        Serial.println(WiFi.localIP());
        _connectedCallback(this);
    } else {
        Serial.println("");
        Serial.println("CANNOT connect to WiFi");
    }
};

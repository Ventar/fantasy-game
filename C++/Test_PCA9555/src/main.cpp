#include <Arduino.h>
#include <PCA9555.h>

#define I2C_ADDRESS 0x21
#define I2C_INTERRUPT D7

PCA9555 mx(I2C_ADDRESS);

void setup() {

  Serial.begin(115200);
  Serial.println("\n\nMultiplexer Test 1.0\n");

  // Initialize the multiplexer
  mx.begin();
  mx.setClock(100000);

  for (int i = 0; i < 16; i++) {
    mx.pinMode(i, INPUT); // set all ports to INPUT mode
  }

  pinMode(I2C_INTERRUPT, INPUT); // interrupt for multiplexer
}

void loop() {

  // check if the interrupt was triggered. The PCA9555 interrupt will change to
  // LOW when a port / pin changes its state. A reset is performed when the
  // register is read the next time
  if (digitalRead(I2C_INTERRUPT) == 0) {

    // we need to query the pin states ones, because the stateOfPin method
    // only access the internal state stored in memory. As a consequence
    // the interrupt is reset.
    mx.pinStates();

    Serial.print("sensor : ");
    for (uint8_t i = 0; i < 16; i++) {
      Serial.print(mx.stateOfPin(i));
      Serial.print(" ");
    }
    Serial.println("");

    Serial.println("\n");
  }

  delay(1000);
}
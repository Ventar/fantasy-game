#include <connection.h>

/**
 * @brief the rotation of the board.
 * 
 */
const BoardRotation rotation = DEGREE_180;

void printSummary(BoardRotation degree)
{
    for (int column = 0; column < FIELD_COUNT; column++) {
        for (int row = 0; row < FIELD_COUNT; row++) {

            Serial.println("-----------------------------------------------------------------------------------------------------------");

            Serial.printf("(%d|%d) - led: %2d | north     : %s, east     : %s, south     : %s, west     : %s\n",
                          column,
                          row,
                          getField(degree, column, row)->pixelNo,
                          getField(degree, column, row)->north.enabled ? "XXXX" : "     ",
                          getField(degree, column, row)->east.enabled ? "XXXX" : "     ",
                          getField(degree, column, row)->south.enabled ? "XXXX" : "     ",
                          getField(degree, column, row)->west.enabled ? "XXXX" : "     ");

            Serial.printf("                | north     : %5d, east     : %5d, south     : %5d, west     : %5d\n",
                          getField(degree, column, row)->north.current,
                          getField(degree, column, row)->east.current,
                          getField(degree, column, row)->south.current,
                          getField(degree, column, row)->west.current);

            Serial.printf("                | north(ref): %5d, east(ref): %5d, south(ref): %5d, west(ref): %5d\n",
                          getField(degree, column, row)->north.reference,
                          getField(degree, column, row)->east.reference,
                          getField(degree, column, row)->south.reference,
                          getField(degree, column, row)->west.reference);
        }
    }

    delay(10000);
}

void setup()
{
    Serial.begin(115200);
    delay(500);
    Serial.println("\n\nStarted SmartBoard...");

    setupNeoPixel();
    setupHalSensors(rotation);
    setupConnectivity();
}

void loop()
{

    readGroups(rotation, false);

    for (int row = 0; row < FIELD_COUNT; row++) {
        for (int column = 0; column < FIELD_COUNT; column++) {

            SmartField *field = getField(rotation, column, row);
            SmartField *prevField = getPreviousField(rotation, column, row);

            if (prevField->north.enabled != field->north.enabled) {
                Serial.printf("[%d][%d] - change detected, ref ::= [%d], current ::= [%d], enabled ::= [%d...\n", column, row, field->north.reference, field->north.current, field->north.enabled);
#ifndef DEBUG_BOARD
                sendStatusUodate(rotation);
                goto CHANGE_DETECTED;
#endif
            }

            if (prevField->east.enabled != field->east.enabled) {
                Serial.printf("[%d][%d] - change detected, ref ::= [%d], current ::= [%d], enabled ::= [%d...\n", column, row, field->east.reference, field->east.current, field->east.enabled);
#ifndef DEBUG_BOARD
                sendStatusUodate(rotation);
                goto CHANGE_DETECTED;
#endif
            }

            if (prevField->south.enabled != field->south.enabled) {
                Serial.printf("[%d][%d] - change detected, ref ::= [%d], current ::= [%d], enabled ::= [%d...\n", column, row, field->south.reference, field->south.current, field->south.enabled);
#ifndef DEBUG_BOARD
                sendStatusUodate(rotation);
                goto CHANGE_DETECTED;
#endif
            }

            if (prevField->west.enabled != field->west.enabled) {
                Serial.printf("[%d][%d] - change detected, ref ::= [%d], current ::= [%d], enabled ::= [%d]...\n", column, row, field->west.reference, field->west.current, field->west.enabled);
#ifndef DEBUG_BOARD
                sendStatusUodate(rotation);
                goto CHANGE_DETECTED;
#endif
            }

#ifdef DEBUG_BOARD

            if (field->north.enabled) {
                setFieldColor(rotation, column, row, 0, 0, 255);
            } else if (field->east.enabled) {
                setFieldColor(rotation, column, row, 255, 255, 0);
            } else if (field->south.enabled) {
                setFieldColor(rotation, column, row, 255, 0, 0);
            } else if (field->west.enabled) {
                setFieldColor(rotation, column, row, 0, 255, 0);
            } else {
                setFieldColor(rotation, column, row, 0, 0, 0);
            }

            strip.show();
#endif
        }
    }

#ifndef DEBUG_BOARD
CHANGE_DETECTED:
#endif

    memcpy(prevFields, fields, FIELD_COUNT * FIELD_COUNT * sizeof(SmartField));

#ifdef PRINT_SUMMARY
    printSummary(rotation);
#endif

#ifndef DEBUG_BOARD
    MDNS.update();
    handleUDP(rotation);
#endif

/*

    for (int row = 0; row < FIELD_COUNT; row++) {
        for (int column = 0; column < FIELD_COUNT; column++) {
            int pixelNo = row * FIELD_COUNT + column;
            fields[column][row].pixelNo = pixelNo;
            strip.setPixelColor(pixelNo, 24, 8, 0);
            strip.show();
        }
    }

    int delayMS = 125;

    SmartField *f1 = getField(rotation, 0, 0);
    SmartField *f2 = getField(rotation, 0, 5);
    SmartField *f3 = getField(rotation, 5, 0);
    SmartField *f4 = getField(rotation, 5, 5);
    SmartField *f5 = getField(rotation, 2, 2);
    SmartField *f6 = getField(rotation, 2, 3);
    SmartField *f7 = getField(rotation, 3, 2);
    SmartField *f8 = getField(rotation, 3, 3);

    strip.setPixelColor(f1->pixelNo, 255, 0, 0);
    strip.setPixelColor(f2->pixelNo, 255, 0, 0);
    strip.setPixelColor(f3->pixelNo, 255, 0, 0);
    strip.setPixelColor(f4->pixelNo, 255, 0, 0);
    strip.show();
    delay(delayMS);

    for (int i = 255; i > 64; i = i - 20) {
        strip.setPixelColor(f1->pixelNo, i - random(15, 64), 0, 0);
        strip.setPixelColor(f2->pixelNo, i - random(15, 64), 0, 0);
        strip.setPixelColor(f3->pixelNo, i - random(15, 64), 0, 0);
        strip.setPixelColor(f4->pixelNo, i - random(15, 64), 0, 0);
        strip.setPixelColor(f5->pixelNo, i - random(15, 64), 0, 0);
        strip.setPixelColor(f6->pixelNo, i - random(15, 64), 0, 0);
        strip.setPixelColor(f7->pixelNo, i - random(15, 64), 0, 0);
        strip.setPixelColor(f8->pixelNo, i - random(15, 64), 0, 0);
        strip.show();
        delay(delayMS);
    }

    for (int i = 64; i < 255; i = i + 50) {
        strip.setPixelColor(f1->pixelNo, i - random(15, 64), 0, 0);
        strip.setPixelColor(f2->pixelNo, i - random(15, 64), 0, 0);
        strip.setPixelColor(f3->pixelNo, i - random(15, 64), 0, 0);
        strip.setPixelColor(f4->pixelNo, i - random(15, 64), 0, 0);
        strip.setPixelColor(f5->pixelNo, i - random(15, 64), 0, 0);
        strip.setPixelColor(f6->pixelNo, i - random(15, 64), 0, 0);
        strip.setPixelColor(f7->pixelNo, i - random(15, 64), 0, 0);
        strip.setPixelColor(f8->pixelNo, i - random(15, 64), 0, 0);
        strip.show();
        delay(delayMS);
    }

    for (int i = 180; i > 64; i = i - 20) {
        strip.setPixelColor(f1->pixelNo, 255, i, 0);
        strip.setPixelColor(f2->pixelNo, 200, i, 0);
        strip.setPixelColor(f3->pixelNo, 210, i, 0);
        strip.setPixelColor(f4->pixelNo, 190, i, 0);
        strip.setPixelColor(f5->pixelNo, 180, i, 0);
        strip.setPixelColor(f6->pixelNo, 220, i, 0);
        strip.setPixelColor(f7->pixelNo, 180, i, 0);
        strip.setPixelColor(f8->pixelNo, 240, i, 0);
        strip.show();
        delay(delayMS);
    }

    for (int i = 128; i > 50; i = i - 20) {
        strip.setPixelColor(f1->pixelNo, i + random(15, 64), 0, 0);
        strip.setPixelColor(f2->pixelNo, i + random(64), 0, 0);
        strip.setPixelColor(f3->pixelNo, i + random(64), 0, 0);
        strip.setPixelColor(f4->pixelNo, i + random(64), 0, 0);
        strip.setPixelColor(f5->pixelNo, i + random(64), 0, 0);
        strip.setPixelColor(f6->pixelNo, i + random(64), 0, 0);
        strip.setPixelColor(f7->pixelNo, i + random(64), 0, 0);
        strip.setPixelColor(f8->pixelNo, i + random(64), 0, 0);
        strip.show();
        delay(delayMS);
    }

    for (int i = 60; i < 150; i = i + 20) {
        strip.setPixelColor(f1->pixelNo, i + random(64), 0, 0);
        strip.setPixelColor(f2->pixelNo, i + random(64), 0, 0);
        strip.setPixelColor(f3->pixelNo, i + random(64), 0, 0);
        strip.setPixelColor(f4->pixelNo, i + random(64), 0, 0);
        strip.setPixelColor(f5->pixelNo, i + random(64), 0, 0);
        strip.setPixelColor(f6->pixelNo, i + random(64), 0, 0);
        strip.setPixelColor(f7->pixelNo, i + random(64), 0, 0);
        strip.setPixelColor(f8->pixelNo, i + random(64), 0, 0);
        strip.show();
        delay(delayMS);
    }

    for (int i = 180; i > 70; i = i - 20) {
        strip.setPixelColor(f1->pixelNo, 150, i + random(64), 0);
        strip.setPixelColor(f2->pixelNo, 150, i + random(64), 0);
        strip.setPixelColor(f3->pixelNo, 150, i + random(64), 0);
        strip.setPixelColor(f4->pixelNo, 150, i + random(64), 0);
        strip.setPixelColor(f5->pixelNo, 150, i + random(64), 0);
        strip.setPixelColor(f6->pixelNo, 150, i + random(64), 0);
        strip.setPixelColor(f7->pixelNo, 150, i + random(64), 0);
        strip.setPixelColor(f8->pixelNo, 150, i + random(64), 0);
        strip.show();
        delay(delayMS);
    }
    */
}
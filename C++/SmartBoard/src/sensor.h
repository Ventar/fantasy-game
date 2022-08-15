#include <Adafruit_NeoPixel.h>
#include <Arduino.h>
#include <PCF8575.h>
#include <Wire.h>
#include <cmath>
#include <setup.h>

// the analog input to read the HAL sensor values
#define ANALOG_IN A0

// the number of fields in a single column and/or row on the board;
// the board will always be a square, i.e. column count == row count.
// a single field will always consist of a LED and 4 sensors to determine
// the direction of an element of it (north, east, south, west) where
// every direction is representet by a single HAL sensor to detect the
// direction of a figure.
#define FIELD_COUNT 6

// the number of groups in a single column and/or row on the board;
// to read the data of the fields 4 fields with 4 HAL sensors each
// (16 sensors in total) are grouped together and access via an
// CD74HC4067 analog multiplexer.
#define GROUP_COUNT 3

/**
 * @brief the rotation of the board.
 * 
 */
enum BoardRotation {
    DEGREE_0 = 0,
    DEGREE_90 = 1,
    DEGREE_180 = 2,
    DEGREE_270 = 3
};

/**
 * @brief spcial effect of the LED
 * 
 */
enum ColorEffect {
    NONE = 0,
    FIXED_COLOR = 1,
    RED_FLAME = 2,
    GREEN_FLAME = 3,
    BLUE_FLAME = 4
};

/**
 * @brief a single sensor value on the board
 * 
 */
struct Sensor {
    /**
     * @brief the current value that is read.
     */
    int current = 0;

    //int currentMin = 0;
    //int currentMax = 0;

    /**
     * @brief the reference value that was read when the board 
     *        was initialized
     */
    int reference = 0;

    /**
     * @brief if the sensor is active or not. This depends on the difference
     *        between the reference value and the current value.
     */
    bool enabled = false;
};

/**
 * @brief a single field on the board that consists of 4 HAL sensors for the 
 *        detection of a character direction and a single LED to give feddback 
 *        tot he player.
 */
struct SmartField {
    int pixelNo = 0;
    ColorEffect effect = NONE;
    Sensor north;
    Sensor east;
    Sensor south;
    Sensor west;

} fields[FIELD_COUNT][FIELD_COUNT], prevFields[FIELD_COUNT][FIELD_COUNT];

/*!
   @brief I2C multiplexer to access the groups of the board and to control 
          which sensor value is read from the selected group.
 */
PCF8575 PCF(0x20);

/**
 * @brief   reads the currently active sensor value
 * @param count the number of measure that should be performed
 * @return  the value
 * @note    the value is read by querying the analog input count times and
            returning the average of all measurements.
 */
int readSensorValue(int count = 0)
{

    if (count == 0) {
        return analogRead(ANALOG_IN);
    } else {
        long measure = 0;

        for (int i = 0; i < count; i++) {
            measure += analogRead(ANALOG_IN);
        }
        measure /= count;
        return measure;
    }
}

/**
 * @brief sets the values for the field with the given row and column.
 * 
 * @param column the column
 * @param row  the row
 * @param values the pointer to the values
 * @param reference if the reference field should be set
 * @note text sets the values for the field with the given row and column.
 */
void setFieldValues(BoardRotation rotation, int column, int row, int *values, boolean ref)
{

    SmartField *f = &fields[column][row];

    // North = North = 0
    // East = East = 3
    // South = South = 2
    // West = West = 1

    // 90°
    // North = West = 1
    // East = North = 0
    // South = East = 3
    // West = South = 2

    // 180°
    // North = South = 2
    // East = West = 1
    // South = North = 0
    // West = East = 3

    // 270°
    // North = East = 3
    // East = South = 2
    // South = West = 1
    // West = North = 0

    int north = 0;
    int east = 0;
    int south = 0;
    int west = 0;

    switch (rotation) {
    case DEGREE_0:
        north = values[0];
        east = values[3];
        south = values[2];
        west = values[1];
        break;
    case DEGREE_90:
        north = values[1];
        east = values[0];
        south = values[3];
        west = values[2];
        break;
    case DEGREE_180:
        north = values[2];
        east = values[1];
        south = values[0];
        west = values[3];
        break;
    case DEGREE_270:
        north = values[3];
        east = values[2];
        south = values[1];
        west = values[0];
        break;
    }

    if (ref) {

        if (!f->north.enabled) {
            f->north.reference = north;
            f->north.current = 0;
        }

        if (!f->west.enabled) {
            f->west.reference = west;
            f->west.current = 0;
        }

        if (!f->south.enabled) {
            f->south.reference = south;
            f->south.current = 0;
        }

        if (!f->east.enabled) {
            f->east.reference = east;
            f->east.current = 0;
        }

        return;
    }

    f->north.current = north;
    f->west.current = west;
    f->south.current = south;
    f->east.current = east;

    f->north.enabled = (f->north.current > f->north.reference * 1.035 && f->north.current > 100);
    f->west.enabled = (f->west.current > f->west.reference * 1.035 && f->west.current > 100);
    f->south.enabled = (f->south.current > f->south.reference * 1.035 && f->south.current > 100);
    f->east.enabled = (f->east.current > f->east.reference * 1.035 && f->east.current > 100);
}

/*!
    @brief   reads the Hal sensor values into the field struct
    @param   ref  if the data should be stored in the reference fields
 */
void readGroups(BoardRotation rotation, bool ref = false)
{

    //#define GROUP_0 1 // 2^0  PCF 0
    //#define GROUP_1 2 // 2^1  PCF 1
    //#define GROUP_2 4 // 2^2  PCF 2
    //#define GROUP_3 8 // 2^3  PCF 3

    //#define GROUP_4 16  // 2^4  PCF 4
    //#define GROUP_5 32  // 2^5  PCF 5
    //#define GROUP_6 64  // 2^6  PCF 6
    //#define GROUP_7 128 // 2^7  PCF 7

    //#define GROUP_8 256  // 2^8  PCF 8
    //#define EMPTY_0 512  // 2^9  PCF 9
    //#define EMPTY_1 1024 // 2^10 PCF 10
    //#define EMPTY_2 2048 // 2^11 PCF 11

    //#define MULTI_0 4096  // 2^12 PCF 12
    //#define MULTI_1 8192  // 2^13 PCF 13
    //#define MULTI_2 16384 // 2^14 PCF 14
    //#define MULTI_3 32768 // 2^15 PCF 15

    // iterate over all groups to read the data

    int values[16];

    for (int row = 0; row < GROUP_COUNT; row++) {
        for (int column = 0; column < GROUP_COUNT; column++) {

            // see https://stackoverflow.com/questions/18591924/how-to-use-bitmask

            int group = (column) + (row * GROUP_COUNT); // calculate the group based on the row and column
            uint16_t groupMask = 0x0FFF;                // all groups are high, empty fields are high, analog multiplexer input 0 is selected
            groupMask &= ~((uint16_t)pow(2, group));    // switch the group output to low to trigger the transistor for
                                                        // the power supply of the group.

            PCF.write16(groupMask);
            delay(10);
            // read the analog values of the HAL sensors.

            for (uint16_t i = 0; i < 16; i++) {
                uint16_t mask = groupMask | (i << 12);
                PCF.write16(mask);

                if (ref) {
                    values[i] = readSensorValue(20);
                } else {
                    values[i] = readSensorValue(10);
                }
            }

            // update the smart fields with the read values.

            int row2x = row * 2;
            int column2x = column * 2;

            setFieldValues(rotation, column2x, row2x, values, ref);              // Field A of the group
            setFieldValues(rotation, column2x + 1, row2x, &values[4], ref);      // Field B of the group
            setFieldValues(rotation, column2x, row2x + 1, &values[8], ref);      // Field C of the group
            setFieldValues(rotation, column2x + 1, row2x + 1, &values[12], ref); // Field D of the group
        }
    }
}

/**!
   @brief returns the field based on the passed parameters. 
   
   @param degree 
   @param column 
   @param row 
   
   @note returnd the field based onthe passed parameter. The fields are calculated based on the degrees:
   
    0° 
    ------------------------------
    5/0 5/1 5/2 5/3 5/4 5/5
    4/0 4/1 4/2 4/3 4/4 4/5
    3/0 3/1 3/2 3/3 3/4 3/5
    2/0 2/1 2/2 2/3 2/4 2/5
    1/0 1/1 1/2 1/3 1/4 1/5
    0/0 0/1 0/2 0/3 0/4 0/5 
   
    90° 
    ------------------------------
    0/0 1/0 2/0 3/0 4/0 5/0
    0/1 1/1 2/1 3/1 4/1 5/1
    0/2 1/2 2/2 3/2 4/2 5/2
    0/3 1/3 2/3 3/3 4/3 5/3
    0/4 1/4 2/4 3/4 4/5 5/4
    0/5 1/5 2/5 3/5 4/5 5/5
   
    column_new = 5 - row_old
    row_new = column_old
    
    old = 2/3 -> 3/3
    old = 3/5 -> 5/2
    old = 4/1 -> 1/1
   
    180° 
    ------------------------------
    0/5 0/4 0/3 0/2 0/1 0/0
    1/5 1/4 1/3 1/2 1/1 1/0
    2/5 2/4 2/3 2/2 2/1 2/0
    3/5 3/4 3/3 3/2 3/1 3/0
    4/5 4/4 4/3 4/2 4/1 4/0
    5/5 5/4 5/3 5/2 5/1 5/0
   
    column_new = 5 - column_old
    row_new = 5 - row_old
    
    old = 2/3 -> 3/2
    old = 3/5 -> 2/0
    old = 4/1 -> 1/4
   
    270° 
    ------------------------------
    5/5 4/5 3/5 2/5 1/5 0/5
    5/4 4/4 3/4 2/4 1/4 0/4
    5/3 4/3 3/3 2/3 1/3 0/3
    5/2 4/2 3/2 2/2 1/2 0/2
    5/1 4/1 3/1 2/1 1/1 0/1
    5/0 4/0 3/0 2/0 1/0 0/0
   
    column_new = row_old
    row_new = 5 - column_old
    
    old = 2/3 -> 3/2
    old = 3/5 -> 0/3
    old = 4/1 -> 4/4
   
   @return SmartField* the field

 */
SmartField *getField(BoardRotation degree, int column, int row)
{
    switch (degree) {
    case DEGREE_0:
        return &fields[column][row];
        break;
    case DEGREE_90:
        return &fields[FIELD_COUNT - 1 - row][column];
        break;
    case DEGREE_180:
        return &fields[FIELD_COUNT - 1 - column][FIELD_COUNT - 1 - row];
        break;
    case DEGREE_270:
        return &fields[row][FIELD_COUNT - 1 - column];
        break;
    }

    return 0;
}

SmartField *getPreviousField(BoardRotation degree, int column, int row)
{
    switch (degree) {
    case DEGREE_0:
        return &prevFields[column][row];
        break;
    case DEGREE_90:
        return &prevFields[FIELD_COUNT - 1 - row][column];
        break;
    case DEGREE_180:
        return &prevFields[FIELD_COUNT - 1 - column][FIELD_COUNT - 1 - row];
        break;
    case DEGREE_270:
        return &prevFields[row][FIELD_COUNT - 1 - column];
        break;
    }

    return 0;
}

/**
 * @brief performs the setup of the hal sensors
 * @param rotation the board rotation
 * 
 */
void setupHalSensors(BoardRotation rotation)
{
    // Analo input to read the sensor values
    pinMode(A0, INPUT);

    // initialice the PCF8575
    boolean connected = PCF.begin(D1, D2, PCF8575_INITIAL_VALUE);
    delay(2000);

    // initialize the fields with the HAL sensor values
    for (int i = 0; i < 2; i++) {
        readGroups(rotation, true);
        delay(500);
    }

    memcpy(prevFields, fields, FIELD_COUNT * FIELD_COUNT * sizeof(SmartField));

    Serial.print("PCF8575 Connected: ");
    Serial.println(connected == 1 ? "true" : "false");
}
package mro.fantasy.applications.simulator.board;

import mro.fantasy.game.devices.board.BoardControllerEventListener;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * The physical game board has 4 HAL sensors (detect magnetic fields) for every field and 4 magnets to keep tiles on top of the fields in place. The scenic tiles which are placed
 * on top of a field have the same 4 magnets to keep them in place and one or more additional ones to activate the HAL sensors.
 * <pre>{@code
 *    Field                     Player Scenic Tile
 *   ┌─────────────────────┐    ┌─────────────────────┐
 *   │ o      NORTH      o │    │ o                 o │
 *   │         ███         │    │                     │
 *   │ W                 E │    │                     │
 *   │ E █    (5|8)    █ A │    │                     │
 *   │ S █             █ S │    │                     │
 *   │ T                 T │    │                     │
 *   │         ███         │    │         ooo         │
 *   │ o      SOUTH      o │    │ o                 o │
 *   └─────────────────────┘    └─────────────────────┘
 * }</pre>
 * If you place the example player tile on top of the field now (as shown above) the southern sensor of the field would be activated and this event listener would be triggered as a
 * result with an {@link BoardControllerEventListener.FieldUpdate} of column 5, row 8, northern false, eastern false, southern true and western false.
 * <p>
 * Some tiles have more than one magnet. This allows the detection in case they are spawning multiple fields. Examples for such tiles are scenic ones like crates that have a
 * dimension of 2x1 or monsters with a size of 2x2. Based on the magnets on these tiles the engine is able to detect the correct placement of such tiles.
 * <p>
 * In addition, every field has an LED that can be turned on or off in various colors. This LED resides in the middle of the 4 sensors.
 *
 * <pre>{@code
 * Legend:
 * ─────────────────────────────────────────────
 *   █  - HAL sensor
 *   o  - magnet
 * ─────────────────────────────────────────────
 * }</pre>
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-21
 */
public class BoardField {

    /**
     * The x coordinate of the field in the frame coordinate system.
     */
    private int x;

    /**
     * The y coordinate of the field in the frame coordinate system.
     */
    private int y;

    /**
     * The size in pixel of the field (which is a square)
     */
    private int s;

    /**
     * The column of the field.
     */
    private int column;

    /**
     * The row of the field.
     */
    private int row;

    /**
     * The actual field.
     */
    private final Rectangle field;

    /**
     * The rectangle that represents the LEDs.
     */
    private final Rectangle led;

    /**
     * The polygon that represents the northern HAL sensor.
     */
    private final Polygon north;

    /**
     * The polygon that represents the eastern HAL sensor.
     */
    private final Polygon east;

    /**
     * The polygon that represents the southern HAL sensor.
     */
    private final Polygon south;

    /**
     * The polygon that represents the western HAL sensor.
     */
    private final Polygon west;

    /**
     * If northern HAL sensor is activated by a magnet.
     */
    private boolean northEnabled;

    /**
     * If eastern HAL sensor is activated by a magnet.
     */
    private boolean eastEnabled;


    /**
     * If southern HAL sensor is activated by a magnet.
     */
    private boolean southEnabled;

    /**
     * If western HAL sensor is activated by a magnet.
     */
    private boolean westEnabled;

    /**
     * The color of the LED {@code null} means of and is the same as @{@link Color#DARK_GRAY}
     */
    private Color ledColor;

    /**
     * Last time the
     */
    private long lastChange;

    /**
     * Creates a new field based on the column (x) and row(y) of this field on the board.
     *
     * @param column the column of the field
     * @param row    the row of the field
     */
    BoardField(int column, int row) {
        this.x = column * Configuration.FIELD_SIZE_PX;
        this.y = row * Configuration.FIELD_SIZE_PX;
        this.column = column;
        this.row = row;
        this.s = Configuration.FIELD_SIZE_PX;
        field = new Rectangle(x + 2, y + 2, s - 2, s - 2);
        north = createSensorPolygon(p(8, 8), p(28, 28), p(s - 28, 28), p(s - 8, 8));
        east = createSensorPolygon(p(s - 8, 8), p(s - 28, 28), p(s - 28, s - 28), p(s - 8, s - 8));
        south = createSensorPolygon(p(8, 8), p(28, 28), p(28, s - 28), p(8, s - 8));
        west = createSensorPolygon(p(8, s - 8), p(28, s - 28), p(s - 28, s - 28), p(s - 8, s - 8));
        led = new Rectangle(x + 33, y + 33, s - 66, s - 66);
    }

    /**
     * Creates a new sensor field
     *
     * @param p1 the first point
     * @param p2 the second point
     * @param p3 the third point
     * @param p4 the fourth point
     *
     * @return the polygon that defines the sensor
     */
    private Polygon createSensorPolygon(Point p1, Point p2, Point p3, Point p4) {
        return new Polygon(
                new int[]{x + p1.x, x + p2.x, x + p3.x, x + p4.x},
                new int[]{y + p1.y, y + p2.y, y + p3.y, y + p4.y},
                4);
    }

    /**
     * Creates a new Point.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     *
     * @return the point
     */
    private Point p(int x, int y) {
        return new Point(x, y);
    }

    /**
     * Paints the field to the passed graphic context
     *
     * @param g2d           the graphic context.
     * @param activePolygon the currently active polygon on the {@link BoardFrame}
     */
    void paint(Graphics2D g2d, Polygon activePolygon) {
        g2d.setColor(Configuration.FIELD_BACKGROUND);
        g2d.fillRect(field.x, field.y, field.width, field.height);
        drawSensor(g2d, north, activePolygon, northEnabled);
        drawSensor(g2d, east, activePolygon, eastEnabled);
        drawSensor(g2d, south, activePolygon, southEnabled);
        drawSensor(g2d, west, activePolygon, westEnabled);

        if (ledColor == null || ledColor.equals(Color.BLACK)) {
            g2d.setColor(Configuration.FIELD_BACKGROUND);
        } else {
            g2d.setColor(ledColor);
        }

        g2d.fillRect(led.x, led.y, led.width, led.height);

    }

    /**
     * Returns all polygons of this field.
     *
     * @return all polygons
     */
    List<Polygon> getPolygons() {
        return Arrays.asList(north, east, south, west);
    }


    /**
     * Draws a new sensor field
     *
     * @param g2d           the graphic context to draw
     * @param polygon       the polygon to draw
     * @param activePolygon the currently active polygon on the {@link BoardFrame}
     *
     * @return the polygon that defines the sensor
     */
    private void drawSensor(Graphics2D g2d, Polygon polygon, Polygon activePolygon, boolean enabled) {
        if (activePolygon != null && polygon.equals(activePolygon)) {
            g2d.setColor(Configuration.POLYGON_HOVER);
        } else if (enabled) {
            g2d.setColor(Configuration.POLYGON_SELECTED);
        } else {
            g2d.setColor(Configuration.FIELD_BACKGROUND);
        }
        g2d.fillPolygon(polygon);
        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawPolygon(polygon);
        g2d.setStroke(new BasicStroke(1));
    }

    /**
     * Checks if the click changes the status of a polygon on this field.
     *
     * @param point the point to check
     */
    public void onClick(Point point) {

        if (north.contains(point.getX(), point.getY())) {
            northEnabled = !northEnabled;
            lastChange = System.currentTimeMillis();
        } else if (east.contains(point.getX(), point.getY())) {
            eastEnabled = !eastEnabled;
            lastChange = System.currentTimeMillis();
        } else if (south.contains(point.getX(), point.getY())) {
            southEnabled = !southEnabled;
            lastChange = System.currentTimeMillis();
        } else if (west.contains(point.getX(), point.getY())) {
            westEnabled = !westEnabled;
            lastChange = System.currentTimeMillis();
        } else if (northEnabled || eastEnabled || southEnabled || westEnabled) {
            northEnabled = false;
            eastEnabled = false;
            southEnabled = false;
            westEnabled = false;
            lastChange = System.currentTimeMillis();
        }

    }

    /**
     * Returns a single byte that has all sensor states encoded:
     * <pre>{@code
     *
     *  with sensor state
     *  bit  -  | 7 6 5 4   3     2     1     0      |
     *  data -  | <empty>   west  south east  north  |
     *
     * }</pre>
     *
     * @return the sensor states in a single byte
     */
    public byte getSensorState() {
        byte data = (byte) (northEnabled ? 1 : 0);
        data |= (eastEnabled ? 1 : 0) << 1;
        data |= (southEnabled ? 1 : 0) << 2;
        data |= (westEnabled ? 1 : 0) << 3;
        return data;
    }

    /**
     * Returns the timestamp of the last change of the sensor values for this field. The last change time is modified if the {@link #onClick(Point)} changes the state of a field
     * sensor
     *
     * @return the last change time or 0 if no change happened
     */
    public long getLastChange() {
        return lastChange;
    }

}


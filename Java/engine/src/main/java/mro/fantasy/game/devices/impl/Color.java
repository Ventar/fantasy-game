package mro.fantasy.game.devices.impl;

/**
 * Color of a LED
 *
 * @author Michael Rodenbuecher
 * @since 2021-12-16
 */
public record Color(int red, int green, int blue) {

    public static final Color NOT_SET = new Color(255, 255, 255);
    public static final Color OFF = new Color(0, 0, 0);
    public static final Color RED = new Color(255, 0, 0);
    public static final Color GREEN = new Color(0, 255, 0);
    public static final Color BLUE = new Color(0, 0, 255);
    public static final Color BLUE_50 = new Color(0, 0, 255);
    public static final Color YELLOW = new Color(255, 255, 0);

    public static final Color SETUP_PLAN_TILE_ANCHOR = new Color(128, 0, 0);
    public static final Color PLAN_TILE_FIELD = new Color(0, 128, 0);
    public static final Color PLAN_TILE_DIRECTION = new Color(255, 220, 0);

    public static final Color MOVEMENT_START = new Color(128, 0, 128);
    public static final Color MOVEMENT_COLOR = new Color(0, 128, 0);

    public static final Color VISIBILITY_FALSE = new Color(128, 0, 0);
    public static final Color VISIBILITY_TRUE = new Color(128, 128, 0);
    public static final Color VISIBILITY_BLOCKED = new Color(0, 0, 128);
    public static final Color VISIBILITY_START_FIELD = new Color(64, 0, 64);

    /**
     * Creates a color from the 32bit representation
     *
     * @param rgb the 32bit value
     */
    public Color(final int rgb) {
        this((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    /**
     * Returns the 32bit integer that represents this color.
     *
     * @return the integer
     */
    public int getInt() {
        int rgb = red;
        rgb = (rgb << 8) + green;
        rgb = (rgb << 8) + blue;
        return rgb;
    }

    public Color clone() {
        return new Color(red, green, blue);
    }


    @Override
    public String toString() {
        return "(" + red + "," + green + "," + blue + ")";
    }
}




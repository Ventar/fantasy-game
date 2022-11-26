package mro.fantasy.game.devices.impl;

/**
 * Effect on the game board for the LEDs.
 *
 * @author Michael Rodenbuecher
 * @since 2021-12-27
 */
public enum ColorEffect {

    FIXED_COLOR(1),
    RED_FLAME(2),
    GREEN_FLAME(3),
    BLUE_FLAME(4);

    private int id;

    ColorEffect(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

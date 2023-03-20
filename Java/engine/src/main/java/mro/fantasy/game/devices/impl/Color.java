package mro.fantasy.game.devices.impl;

/**
 * Color of a LED
 *
 * @author Michael Rodenbuecher
 * @since 2021-12-16
 */
public enum Color {

    Black(0),
    SlateGrey(1),
    Gray(2),
    LightGrey(3),
    White(4),
    DarkRed(5),
    Crimson(6),
    Red(7),
    Coral(8),
    OrangeRed(9),
    Orange(10),
    Chocolate(11),
    Yellow(12),
    Gold(13),
    DarkGreen(14),
    GreenYellow(15),
    LawnGreen(16),
    Lime(17),
    LightGreen(18),
    SpringGreen(19),
    LightSeaGreen(20),
    Teal(21),
    Aqua(22),
    Turquoise(23),
    AquaMarine(24),
    PowderBlue(25),
    SteelBlue(26),
    DeepSkyBlue(28),
    SkyBlue(29),
    MidnightBlue(30),
    DarkBlue(31),
    MediumBlue(32),
    Blue(33),
    RoyalBlue(34),
    BlueViolet(35),
    Indigo(36),
    MediumPurple(37),
    DarkMagenta(38),
    DarkViolet(39),
    Magenta(40),
    Orchid(41),
    DeepPink(42),
    Pink(43),
    LightGoldenRodYellow(44),
    MintCream(45);

    /**
     * The unique ID of the color that has to match the ID for the board module devices.
     */
    private final int id;

    /**
     * Creates a new color
     *
     * @param id the unique ID of the color
     */
    Color(final int id) {
        this.id = id;
    }

    /**
     * Returns the unique ID that represents this color.
     *
     * @return the integer
     */
    public int getID() {
        return id;
    }

}




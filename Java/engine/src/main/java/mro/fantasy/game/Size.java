package mro.fantasy.game;

/**
 * The size of something in columns and rows.
 */
public record Size(int columns, int rows) {

    @Override
    public String toString() {
        return columns + "x" + rows;
    }
}

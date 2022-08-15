package mro.fantasy.game;

/**
 * The position of something in columns and rows.
 */
public record Position(int column, int row) {

    @Override
    public String toString() {
        return "(" + column + "|" + row + ')';
    }
}

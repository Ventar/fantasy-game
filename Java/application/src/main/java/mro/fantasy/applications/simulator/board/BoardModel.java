package mro.fantasy.applications.simulator.board;

import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The data model of the board. Since we only have a single board here we can use a Spring component instead of a data object. Therefor injection into other components is
 * possible.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-21
 */
@Component
public class BoardModel {

    /**
     * The color for each field on the board.
     */
    private BoardField[][] fields = new BoardField[Configuration.COLUMNS][Configuration.ROWS];

    public BoardModel() {
        // fill the field matrix
        for (int column = 0; column < Configuration.COLUMNS; column++) {
            for (int row = 0; row < Configuration.ROWS; row++) {
                fields[column][row] = new BoardField(column, row);
            }
        }
    }

    /**
     * Returns the fields of the model.
     *
     * @return
     */
    public BoardField[][] getFields() {
        return fields;
    }

    /**
     * Returns the field at the given column and row.
     *
     * @param column the column
     * @param row    the row
     *
     * @return the field
     */
    public BoardField get(int column, int row) {
        return fields[column][row];
    }

    /**
     * Sets the color of the LED at the given column and row.
     *
     * @param column the row
     * @param row    the column
     * @param color  the color to set
     */
    public void setColor(int column, int row, Color color) {
        fields[column][row].setLedColor(color);
    }

    /**
     * Clears all colors of the board.
     */
    public void clearColors() {
        for (int column = 0; column < Configuration.COLUMNS; column++) {
            for (int row = 0; row < Configuration.ROWS; row++) {
                fields[column][row].setLedColor(Color.BLACK);
            }
        }
    }

    /**
     * Returns the polygon of the HAL sensor for the given x and y coordinates where x and y are the pixels in the coordinate system of the board.
     *
     * @param x the x value
     * @param y the y value
     *
     * @return the Polygon
     */
    public Polygon resolve(int x, int y) {
        // iterate over all fields in the matrix and check if one the
        // contained polygons intersects with the mouse pointer.
        return stream()
                .flatMap(field -> field.getPolygons().stream())              // take the polygons
                .filter(p -> p.contains(x, y))                               // and filter them
                .findAny()
                .orElse(null);
    }

    /**
     * Returns a stream of all {@link BoardField}s in this model.
     *
     * @return the stream
     */
    public Stream<BoardField> stream() {
        return Arrays.stream(fields)                                               // for all columns
                .flatMap(colEntry -> Arrays.stream(colEntry));                     // for all rows
    }

    /**
     * Returns all fields of the model that have changed since the passed timestamp.
     *
     * @param since the modification timestamp
     *
     * @return the list of modified fields
     */
    public List<BoardField> getChangedFields(long since) {
        return Arrays.stream(fields)                               // for all columns
                .flatMap(colEntry -> Arrays.stream(colEntry))      // and all rows
                .filter(f -> f.getLastChange() > since)            // find the fields which were modified after the passed date
                .collect(Collectors.toList());
    }

}

package mro.fantasy.game.devices.board.impl;

import mro.fantasy.game.Position;
import mro.fantasy.game.devices.board.BoardField;
import mro.fantasy.game.devices.board.BoardModule;
import mro.fantasy.game.devices.impl.Color;
import mro.fantasy.game.utils.Condition;

import java.io.IOException;

/**
 * A utility class to visualize a board module for testing purposes. The model and view are combined in one class here.
 * <p>
 * The coordinate system is zero based and starts in the lower right corner of the model:
 *
 * <pre>{@code
 * ┌─────────┬─────────┬─────────┬─────────┐
 * │         │         │         │         │
 * │   0/4   │   1/4   │   2/4   │   3/4   │
 * │         │         │         │         │
 * ├─────────┼─────────┼─────────┼─────────┤
 * │         │         │         │         │
 * │   0/3   │   1/3   │   2/3   │   3/3   │
 * │         │         │         │         │
 * ├─────────┼─────────┼─────────┼─────────┤
 * │         │         │         │         │
 * │   0/2   │   1/2   │   2/2   │   3/2   │
 * │         │         │         │         │
 * ├─────────┼─────────┼─────────┼─────────┤
 * │         │         │         │         │
 * │   0/1   │   1/1   │   2/2   │   3/3   │
 * │         │         │         │         │
 * ├─────────┼─────────┼─────────┼─────────┤
 * │         │         │         │         │
 * │   0/0   │   1/0   │   2/0   │   3/0   │
 * │         │         │         │         │
 * └─────────┴─────────┴─────────┴─────────┘
 * }</pre>
 *
 * @author Michael Rodenbuecher
 * @since 2022-11-18
 */
public class BoardModuleRenderer {

    /**
     * If the sensor is active
     */
    public static final String SENSOR_ON = "█";

    /**
     * The LED of the field is on.
     */
    public static final String LED_ON = "░";

    /**
     * Character width of a rendered field.
     */
    public static final int CELL_WIDTH = 9;

    /**
     * Character height of a field.
     */
    public static final int CELL_HEIGHT = 3;


    /**
     * Renders the 2D model to a String with simple UTF-8 characters.
     * <p>
     * When using this class to generate output you need to ensure that you select the correct font to render the output since not every monospaced font is really monospaced on
     * every character may have a different width. The code was developed with the JetBrains Community Edition and the standard font, if you need to adjust the output you can
     * change the static values of this class.
     *
     * @param module the module to render
     */
    public void render(BoardModule module) {
        StringBuilder buf = new StringBuilder();

        for (int row = 0; row < module.getSize().rows(); row++) {
            if (row == 0) renderRowHeader(module, buf, "┌", "┬", "┐");  // first row has a different row header
            else renderRowHeader(module, buf, "├", "┼", "┤");

            for (int x = 0; x < CELL_HEIGHT; x++) {
                renderRowContent(module, buf, row, x, "│", "│", "│");
            }
        }

        renderRowHeader(module, buf, "└", "┴", "┘");

        buf.append("   ");
        for (int column = 0; column < module.getSize().columns(); column++) {
            buf.append(" ".repeat(CELL_WIDTH / 2));
            buf.append(String.format("%02d", column));
            buf.append(" ".repeat((CELL_WIDTH / 2)));
        }

        buf.append("\n\n");
        buf.append(SENSOR_ON);
        buf.append(" : HAL sensor is active\n");
        buf.append(LED_ON);
        buf.append(" : LED is illuminated\n");

        System.out.println(buf.toString()); //NOSONAR
    }

    /**
     * Appends a row header with the given delimiters.
     *
     * @param module  the module to render
     * @param row     the row on the plan
     * @param cellRow the sub row based on the {@link #CELL_HEIGHT}
     * @param start   the start character
     * @param middle  the middle character
     * @param end     the end character
     * @param buf     the buffer to append the data to
     */
    @SuppressWarnings("java:S107")
    private void renderRowContent(BoardModule module, StringBuilder buf, int row, int cellRow, String start, String middle, String end) {

        if (cellRow == 1) {                                                            // render the row number in front of the ASCII row
            buf.append(String.format("%02d ", module.getSize().rows() - row - 1));     // we need to convert the fields because the renderer renders the field
        } else {                                                                       // top down and the plan has coordinates bottom up
            buf.append("   ");
        }

        buf.append(start);


        for (int column = 0; column < module.getSize().columns(); column++) {

            Position position = new Position(column, module.getSize().rows() - row - 1);  /* we need to convert the fields because the renderer renders the field
                                                                                               top down and the plan has coordinates bottom up */

            BoardField field = module.getField(position);

            buf.append(" ");

            String fill = field.getColor().equals(Color.OFF) ? " " : LED_ON;


            switch (cellRow) {
                case 0:
                    if (field.isNorthEnabled()) {
                        buf.append(fill.repeat(CELL_WIDTH / 2 - 1));
                        buf.append(SENSOR_ON);
                        buf.append(fill.repeat(CELL_WIDTH / 2 - 1));
                    } else {
                        buf.append(fill.repeat(CELL_WIDTH - 2));
                    }
                    break;
                case 1:
                    if (field.isEastEnabled() && field.isWestEnabled()) {
                        buf.append(SENSOR_ON);
                        buf.append(fill.repeat(CELL_WIDTH - 4));
                        buf.append(SENSOR_ON);
                    } else if (field.isEastEnabled()) {
                        buf.append(SENSOR_ON);
                        buf.append(fill.repeat(CELL_WIDTH - 3));
                    } else if (field.isWestEnabled()) {
                        buf.append(fill.repeat(CELL_WIDTH - 3));
                        buf.append(SENSOR_ON);
                    } else {
                        buf.append(fill.repeat(CELL_WIDTH - 2));
                    }
                    break;
                case 2:
                    if (field.isSouthEnabled()) {
                        buf.append(fill.repeat(CELL_WIDTH / 2 - 1));
                        buf.append(SENSOR_ON);
                        buf.append(fill.repeat(CELL_WIDTH / 2 - 1));
                    } else {
                        buf.append(fill.repeat(CELL_WIDTH - 2));
                    }
                    break;
            }

            buf.append(" ");

            Condition.of(column < module.getSize().columns() - 1)        // either append the middle String or the end one depending on
                    .ifTrue(() -> buf.append(middle))                 // the column which is rendered
                    .orElse(() -> buf.append(end));
        }

        buf.append("\n");

    }

    /**
     * Appends a row header with the given delimiters.
     *
     * @param module the module to render
     * @param start  the start character
     * @param middle the middle character
     * @param end    the end character
     * @param buf    the buffer to append the data to
     */
    private void renderRowHeader(BoardModule module, StringBuilder buf, String start, String middle, String end) {
        buf.append("   ");
        buf.append(start);

        for (int column = 0; column < module.getSize().columns(); column++) {
            buf.append("─".repeat(CELL_WIDTH));
            Condition.of(column < module.getSize().columns() - 1)       // either append the middle String or the end one depending on
                    .ifTrue(() -> buf.append(middle))                   // the column which is rendered
                    .orElse(() -> buf.append(end));
        }

        buf.append("\n");
    }

    public static void main(String[] args) throws IOException {
        BoardModule module = new BoardModuleImpl("A1", null, 0);
        module.getField(new Position(3, 2)).setNorthEnabled(true);
        module.getField(new Position(0, 2)).setWestEnabled(true);
        module.getField(new Position(0, 2)).setEastEnabled(true);
        module.getField(new Position(1, 5)).setSouthEnabled(true);

        module.getField(new Position(4, 2)).setWestEnabled(true);
        module.getField(new Position(4, 1)).setEastEnabled(true);
        module.getField(new Position(4, 1)).setColor(Color.RED);
        module.getField(new Position(5, 4)).setColor(Color.RED);

        BoardModuleRenderer renderer = new BoardModuleRenderer();
        renderer.render(module);

    }


}

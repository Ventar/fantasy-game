package mro.fantasy.game.plan.impl;

import mro.fantasy.game.Position;
import mro.fantasy.game.plan.Field;
import mro.fantasy.game.plan.Plan;
import mro.fantasy.game.plan.PlanRenderer;
import mro.fantasy.game.plan.Tile;
import mro.fantasy.game.utils.Condition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A utility class to visualize a boards for testing purposes. The model and view are combined in one class here.
 * <p>
 * Internally the class maintains a String matrix where a single character can be assigned to each field of the matrix.
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
 * @since 2022-07-27
 */
public class ASCIIPlanRenderer implements PlanRenderer {

    public enum RenderOptions {
        ANCHOR,
        TEMPLATE_NAME
    }

    /**
     * The plan field is not covered by any {@link Tile}
     */
    public static final String NO_TILE = "x";

    /**
     * Character to render a field that blocks the line of sight.
     */
    public static final String BLOCKS_LINE_OF_SIGHT = "░";

    /**
     * Character to render a field that blocks the line of sight and the movement of the character.
     */
    public static final String BLOCKS_MOVEMENT_AND_LINE_OF_SIGHT = "█";

    /**
     * Character to render a field that blocks the movement.
     */
    public static final String BLOCKS_MOVEMENT = "▓";

    /**
     * Character to render a marked field.
     */
    public static final String MARKER = ".";

    /**
     * Character to render a character field.
     */
    public static final String CHARACTER = "-";

    /**
     * Character width of a rendered field.
     */
    public static final int CELL_WIDTH = 9;

    /**
     * Character height of a field.
     */
    public static final int CELL_HEIGHT = 3;

    @Override
    public void render(Plan plan) {
        render(plan, (RenderOptions[]) null);
    }

    /**
     * Renders the 2D model to a String with simple UTF-8 characters.
     * <p>
     * When using this class to generate output you need to ensure that you select the correct font to render the output of the {@link #render(Plan)} method since not every
     * monospaced font is really monospaced on every character may have a different width. The code was developed with the JetBrains Community Edition and the standard font, if you
     * need to adjust the output you can change the static values of this class:
     * <ul>
     *     <li>{@link #BLOCKS_LINE_OF_SIGHT}</li>
     *     <li>{@link #BLOCKS_MOVEMENT}</li>
     *     <li>{@link #BLOCKS_MOVEMENT_AND_LINE_OF_SIGHT}</li>
     *     <li>{@link #MARKER}</li>
     *     <li>{@link #CELL_HEIGHT}</li>
     *     <li>{@link #CELL_WIDTH}</li>
     * </ul>
     *
     * @param plan    the plan to render
     * @param options additional option
     */
    public void render(Plan plan, RenderOptions... options) {
        StringBuilder buf = new StringBuilder();

        for (int row = 0; row < plan.getSize().rows(); row++) {
            if (row == 0) renderRowHeader(plan, buf, "┌", "┬", "┐");  // first row has a different row header
            else renderRowHeader(plan, buf, "├", "┼", "┤");

            for (int x = 0; x < CELL_HEIGHT; x++) {
                renderRowContent(plan, buf, row, x, "│", "│", "│", options != null ? Arrays.asList(options) : Collections.emptyList());
            }
        }

        renderRowHeader(plan, buf, "└", "┴", "┘");

        buf.append("   ");
        for (int column = 0; column < plan.getSize().columns(); column++) {
            buf.append(" ".repeat(CELL_WIDTH / 2));
            buf.append(String.format("%02d", column));
            buf.append(" ".repeat((CELL_WIDTH / 2)));
        }

        buf.append("\n\n");
        buf.append(CHARACTER);
        buf.append(" : character field\n");
        buf.append(BLOCKS_LINE_OF_SIGHT);
        buf.append(" : blocks line of sight\n");
        buf.append(BLOCKS_MOVEMENT);
        buf.append(" : blocks movement\n");
        buf.append(BLOCKS_MOVEMENT_AND_LINE_OF_SIGHT);
        buf.append(" : blocks movement and line of sight\n");
        buf.append(MARKER);
        buf.append(" : marker\n");
        buf.append(NO_TILE);
        buf.append(" : field has no tile assigned\n");

        System.out.println(buf.toString()); //NOSONAR
    }

    /**
     * Appends a row header with the given delimiters.
     *
     * @param plan    the plan to render
     * @param row     the row on the plan
     * @param cellRow the sub row based on the {@link #CELL_HEIGHT}
     * @param start   the start character
     * @param middle  the middle character
     * @param end     the end character
     * @param buf     the buffer to append the data to
     * @param options additional render options
     */
    @SuppressWarnings("java:S107")
    private void renderRowContent(Plan plan, StringBuilder buf, int row, int cellRow, String start, String middle, String end, List<RenderOptions> options) {

        if (cellRow == 1) {                                                            // render the row number in front of the ASCII row
            buf.append(String.format("%02d ", plan.getSize().rows() - row - 1));       // we need to convert the fields because the renderer renders the field
        } else {                                                                       // top down and the plan has coordinates bottom up
            buf.append("   ");
        }

        buf.append(start);


        for (int column = 0; column < plan.getSize().columns(); column++) {

            Position position = new Position(column, plan.getSize().rows() - row - 1);  /* we need to convert the fields because the renderer renders the field
                                                                                               top down and the plan has coordinates bottom up */
            Tile tile = plan.getTopTile(position);
            Field field = plan.getTopField(position);

            buf.append(" ");

            if (tile == null) {
                buf.append(NO_TILE.repeat(CELL_WIDTH - 2));
            } else {

                switch (cellRow) {
                    case 0 -> Condition.of(field.isAnchor() && options.contains(RenderOptions.ANCHOR))
                            .ifTrue(() -> {
                                buf.append(tile.getRotation().toDegreeString());
                                buf.append(" ".repeat(CELL_WIDTH - tile.getRotation().toDegreeString().length() - 2));
                            })
                            .orElse(() -> buf.append(getIconString(field, CELL_WIDTH - 2)));

                    case 1 -> Condition.of(options.contains(RenderOptions.TEMPLATE_NAME) || tile.getType().isCharacter())
                            .ifTrue(() -> buf.append(tile.getGameId()).append(" "))
                            .orElse(() -> buf.append(getIconString(field, CELL_WIDTH - 2)));


                    case 2 -> buf.append(getIconString(field, CELL_WIDTH - 2));

                    default -> throw new IllegalStateException("Invalid value for cell rows");
                }
            }

            buf.append(" ");

            Condition.of(column < plan.getSize().columns() - 1)        // either append the middle String or the end one depending on
                    .ifTrue(() -> buf.append(middle))                 // the column which is rendered
                    .orElse(() -> buf.append(end));
        }

        buf.append("\n");

    }

    /**
     * Appends a row header with the given delimiters.
     *
     * @param plan   the plan to render
     * @param start  the start character
     * @param middle the middle character
     * @param end    the end character
     * @param buf    the buffer to append the data to
     */
    private void renderRowHeader(Plan plan, StringBuilder buf, String start, String middle, String end) {
        buf.append("   ");
        buf.append(start);

        for (int column = 0; column < plan.getSize().columns(); column++) {
            buf.append("─".repeat(CELL_WIDTH));
            Condition.of(column < plan.getSize().columns() - 1)       // either append the middle String or the end one depending on
                    .ifTrue(() -> buf.append(middle))                 // the column which is rendered
                    .orElse(() -> buf.append(end));
        }

        buf.append("\n");
    }

    /**
     * Returns a string based on the attributes of the field, i.e. if it blocks movement or los.
     *
     * @param field the field
     * @param count the number of generated characters of the same type
     *
     * @return the string
     */
    private String getIconString(Field field, int count) {

        String icon = " ";

        if (field.getType().isCharacter()) {
            icon = CHARACTER;
        } else if (field.blocksMovement() && field.blocksLineOfSight()) {
            icon = BLOCKS_MOVEMENT_AND_LINE_OF_SIGHT;
        } else if (field.blocksLineOfSight()) {
            icon = BLOCKS_LINE_OF_SIGHT;
        } else if (field.blocksMovement()) {
            icon = BLOCKS_MOVEMENT;
        }

        return icon.repeat(count);

    }

}

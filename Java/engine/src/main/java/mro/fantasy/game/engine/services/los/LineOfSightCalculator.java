package mro.fantasy.game.engine.services.los;

import mro.fantasy.game.Position;
import mro.fantasy.game.engine.plan.Plan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Service class to calculate the distance and visibility of fields in relation to each other.
 * <p>
 * The service uses an underlying abstract {@link Plan} of a plane that consists of multiple rows and columns. Fields can have multiple states, but for this service only one
 * information is relevant: If a field blocks the line of sight (LoS).
 * <p>
 * This service is used to determine if player characters are able to see certain fields in the model or not.
 * <p>
 * Since this calculation is cost intensive the service has an embedded cache that stores LoS calculations between two {@link Position}s on the underlying 2D model. As a result it
 * cannot be reused for multiple parallel sessions but is always bound to the model
 *
 * @author Michael Rodenbuecher
 * @since 2022-01-11
 */
public class LineOfSightCalculator {

    /**
     * Logger
     */
    public static final Logger LOG = LoggerFactory.getLogger(LineOfSightCalculator.class);

    /**
     * Key for stored data to avoid continues recalculation of LoS. If the LoS between two fields was calculated the result is stored in the {@link #equationResults} map so that it
     * can be reused without additional calculations in the future.
     *
     * @author Michael Rodenbuecher
     * @since 2022-01-11
     */
    private record Key(Position startField, Position destField) {
    }

    /**
     * Equation results for LoS calculations.
     */
    private HashMap<Key, List<Position>> equationResults = new HashMap<>();


    /**
     * Returns a list of all visible fields relative to the start field. The algorithm will iterate over all fields in the model (the destination fields) that are in within the
     * limit and calculate the LoS. All visible fields are stored, none visible fields are thrown away. Finally, the visible fields are returned. the LoS between the start field
     * and the
     *
     * @param model      the field model
     * @param startField the start field
     * @param limit      the maximum number of fields that should be taken into account
     *
     * @return the list of visible fields
     */
    public List<Position> getVisibleFields(Plan model, Position startField, int limit) {

        LOG.debug("Calculate visible fields for start field ::= [{}] with limit ::= [{}]", startField, limit);

        Set<Position> result = new HashSet<>();

        // now we can calculate the visibility of all fields for the given tiles.
        model.getFields().forEach(destField -> {
            LOG.trace("---------------------------------------------------------------------------------------");
            LOG.trace("Calculate the line of sight between start field ::= [{}] and destination field ::= [{}]", startField, destField);

            // in case the tile does block the LoS we do not need to take it into account. We cannot check the LoS from the tile directly because it is
            // possible that on a destination position multiple tiles with various LoS rules are available (scenic and floor for example)
            if (!destField.blocksLineOfSight()) {

                // first step is to calculate the distance between the start field and the destination field bf. If the distance is larger
                // then the passed sight the field is not visible and we do not need to take it into account.
                double distance = getDistance(startField, destField.getPosition());

                LOG.trace("Distance between start ::= [{}] and dest ::= [{}] is ::= [{}]", startField, destField, distance);

                // in case the field is out of sight we do not need to calculate the line of sight.
                if (distance < limit + 1) {
                    result.addAll(getFieldsOnLine(model, startField, destField.getPosition()));
                }
            }
        });

        return new ArrayList<>(result);
    }


    /**
     * Returns all fields on the line between the start field and the end field unless the line of sight is blocked. In that case the blocked fields are not returned.
     *
     * @param model      the underlying plan
     * @param startField the start field of the line calculation
     * @param destField  the destination field of the line calculation
     *
     * @return all board fields between start (inclusive) and destination field (inclusive)
     */
    private List<Position> getFieldsOnLine(Plan model, Position startField, Position destField) {

        Key key = new Key(startField, destField);
        Key keyReverse = new Key(destField, startField);

        if (equationResults.containsKey(key)) {  // if we already did that calculation we can reuse the result
            return equationResults.get(key);
        }

        List<Position> resultFields = new ArrayList<>();

        if (startField.equals(destField)) { // easiest case, we can always see our field.
            resultFields.add(new Position(startField.column(), startField.row()));
            equationResults.put(key, resultFields);
            equationResults.put(keyReverse, resultFields);
            return resultFields;
        }

        // Otherwise, the calculation is a little more complex, we build the equation based on the two points and try to figure out
        // which fields are affected between the start field and the end field.

        // Build the equation to calculate fields which build the line of sight between the start field and the destination field.
        //
        //             y2-y1     x2y1-x1y2
        //        y = ——————·x + ————————
        //             x2-x1      x2-x1

        // the x2 - x1 divisor in the equation. If the column is the same this would lead
        // to an invalid null division, i.e. we need to handle that special case
        float divisor = (float) destField.column() - (float) startField.column();
        float m;
        float b;

        if (divisor == 0) {
            m = 1;
            b = 0;
        } else {
            m = (((float) destField.row() - (float) startField.row()) / divisor);
            b = (((float) destField.column() * (float) startField.row() - (float) startField.column() * (float) destField.row()) / divisor);
        }


        LOG.trace("use equation: {}x + {}", m, b);

        // in case m is larger than one (absolute) we have to calculate the rows, otherwise we have to calculate the columns

        if (Math.abs(m) >= 1) {
            int rowsToCalculate = Math.abs(startField.row() - destField.row()); // the number of columns that have to be calculated by the algorithm
            for (int i = 0; i <= rowsToCalculate; i++) {
                float row = startField.row() + (Math.signum(destField.row() - startField.row()) * i);
                float column = (row - b) / m;
                float columnNext = 0;

                if (i < rowsToCalculate + 1) {
                    float rowNext = startField.row() + (Math.signum(destField.row() - startField.row()) * (i + 1));
                    columnNext = (rowNext - b) / m;
                }

                LOG.trace("Result column for row  ::= [{}] is column ::= [{}], column next ::= [{}], -> ({}/{})", row, column, columnNext, Math.round(column), Math.round(row));

                Position posToAdd = new Position(Math.round(column), Math.round(row));

                Position below = new Position(Math.round(column), (int) Math.floor(row));
                Position above = new Position(Math.round(column), (int) Math.ceil(row));
                Position left = new Position((int) Math.floor(column), Math.round(row));
                Position right = new Position((int) Math.ceil(column), Math.round(row));

                if (Math.round(columnNext) != Math.round(column)) {
                    if (model.blocksLineOfSight(left)) {
                        posToAdd = left;
                    } else if (model.blocksLineOfSight(right)) {
                        posToAdd = right;
                    }
                }


                if (startField.column() == destField.column()) {
                    posToAdd = new Position(startField.column(), Math.round(row));
                }

                if (model.blocksLineOfSight(posToAdd)) {
                    break;
                } else {
                    resultFields.add(posToAdd);
                }
            }


        } else {
            int columnsToCalculate = Math.abs(startField.column() - destField.column()); // the number of columns that have to be calculated by the algorithm
            for (int i = 0; i <= columnsToCalculate; i++) {
                float columnModifier = Math.signum(destField.column() - startField.column()) * i;
                float column = startField.column() + columnModifier;
                float row = m * column + b;

                float rowNext = 0;

                if (i < columnsToCalculate + 1) {
                    float columnNext = startField.row() + (Math.signum(destField.column() - startField.column()) * (i + 1));
                    rowNext = m * columnNext + b;
                }

                LOG.trace("Result row for columns ::= [{}] is row ::= [{}]; row next ::= [{}] -> ({}/{})", column, row, rowNext, Math.round(column), Math.round(row));

                Position posToAdd = new Position(Math.round(column), Math.round(row));


                Position below = new Position(Math.round(column), (int) Math.floor(row));
                Position above = new Position(Math.round(column), (int) Math.ceil(row));
                Position left = new Position((int) Math.floor(column), Math.round(row));
                Position right = new Position((int) Math.ceil(column), Math.round(row));

                if (Math.round(rowNext) != Math.round(row)) {
                    if (model.blocksLineOfSight(below)) {
                        posToAdd = below;
                    } else if (model.blocksLineOfSight(above)) {
                        posToAdd = above;
                    }
                }

                if (startField.row() == destField.row()) {
                    posToAdd = new Position(Math.round(column), startField.row());
                }

                if (model.blocksLineOfSight(posToAdd)) {
                    break;
                } else {
                    resultFields.add(posToAdd);
                }
            }
        }

        equationResults.put(key, resultFields);
        equationResults.put(keyReverse, resultFields);
        return resultFields;

    }

    /**
     * Returns the distance between two fields.
     *
     * @param startField the first field
     * @param endField   the second field
     *
     * @return the distance
     */
    private double getDistance(Position startField, Position endField) {

        // How to find the distance between two points?
        //   - Get the coordinates of both points in space.
        //   - Subtract the x-coordinates of one point from the other, same for the y components.
        //   - Square both results separately.
        //   - Sum the values you got in the previous step.
        //   - Find the square root of the result above.

        double xPow = Math.pow(startField.row() - endField.row(), 2);
        double yPow = Math.pow(startField.column() - endField.column(), 2);

        return Math.sqrt(xPow + yPow);

    }


}

package mro.fantasy.game.devices.board.impl;

import mro.fantasy.game.Position;
import mro.fantasy.game.Size;
import mro.fantasy.game.devices.board.BoardField;
import mro.fantasy.game.devices.board.BoardModule;
import mro.fantasy.game.devices.board.GameBoard;
import mro.fantasy.game.devices.events.DeviceMessage;
import mro.fantasy.game.devices.events.DeviceEventHandler;
import mro.fantasy.game.devices.impl.Color;
import mro.fantasy.game.engine.events.BoardUpdatedEvent;
import mro.fantasy.game.engine.events.GameEventListener;
import mro.fantasy.game.engine.events.impl.AbstractGameEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of the game board.
 *
 * @author Michael Rodenbuecher
 * @since 2022-11-23
 */
@Component
public class GameBoardImpl extends AbstractGameEventProducer<BoardUpdatedEvent, GameEventListener<BoardUpdatedEvent>> implements DeviceEventHandler, GameBoard {

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(GameBoardImpl.class);

    /**
     * A position of a {@link BoardField} within a physical module.
     *
     * @param module   the module
     * @param position the position
     */
    private record ModulePosition(BoardModule module, Position position) {
        /**
         * Returns the {@link BoardField} of the set module at the given position in the coordinate system of the board.
         *
         * @return the field
         */
        BoardField getField() {
            return module().getField(position);
        }

        /**
         * Changes the color of the {@link BoardField} specified by the set module and position
         *
         * @param color the color
         */
        void setColor(Color color) {
            module.setColor(position, color);
        }

    }

    /**
     * The complete size of the game board.
     */
    private Size gameBoardSize;

    /**
     * The size (columns and rows) of a single physical module
     */
    private Size moduleSize;

    /**
     * A list of boards which are not arranged in a matrix as in the {@link #moduleMatrix} field.
     */
    private List<BoardModule> boardModules;

    /**
     * The matrix of board modules which build the game board.
     */
    private BoardModule[][] moduleMatrix;


    @Override
    public void handle(DeviceMessage eventData) {

        if (moduleMatrix == null) {
            LOG.debug("[{}] - No board modules were configured, skip event processing...", eventData.getDeviceId());
            return;
        }

        switch (eventData.getEventType()) {
            case BOARD_SENSOR_UPDATE:
                LOG.debug("[{}] - Handle sensor update, try to find board out of :.= [{}]", eventData.getDeviceId(), boardModules.size());

                var deviceId = eventData.getDeviceId();                          // the ID of the physical module
                var data = eventData.getData();
                var fieldCount = Byte.toUnsignedInt(data[0]);                      // the number of fields which have a sensor update
                var fieldUpdate = new ArrayList<BoardUpdatedEvent.FieldUpdate>();       // the updated fields in the coordinate system of the game board
                BoardUpdatedEvent event = () -> fieldUpdate;                            // the event that is fired

                Position modulePosition = null;                                         // the position of the field in the coordinate system of the board module
                byte sensorData = 0;                                                    // the byte with the updated sensor state
                BoardModule module = null;                                              // the module that sent the update event
                Position boardPosition = null;                                          // the position of the field in the coordinate system of the game boarf

                for (int f = 0; f < fieldCount; f++) {         // iterate over all updated fields
                    modulePosition = new Position(data[1 + f * 3], data[2 + f * 3]);
                    sensorData = data[3 + f * 3];

                    // we need to find the module and update the data structure. The position in the event is always in the coordinate system of a single physical board module
                    // and we have to convert that to the coordinate system of the whole game board before sending out the game event.
                    for (int colBoard = 0; colBoard < moduleMatrix.length; colBoard++) {
                        for (int rowBoard = 0; rowBoard < moduleMatrix[0].length; rowBoard++) {
                            module = moduleMatrix[colBoard][rowBoard];

                            if (deviceId.equals(module.getId())) {
                                BoardField field = module.getField(modulePosition);
                                field.setSensorState(sensorData);
                                boardPosition = new Position(
                                        colBoard * module.getSize().columns() + modulePosition.column(),
                                        rowBoard * module.getSize().rows() + modulePosition.row()
                                );

                                LOG.trace("[{}] - updated game board position ::= [{}] with board field: {}", eventData.getDeviceId(), boardPosition, field);

                                fieldUpdate.add(new BoardUpdatedEvent.FieldUpdate(
                                        boardPosition,
                                        field.isNorthEnabled(),
                                        field.isEastEnabled(),
                                        field.isSouthEnabled(),
                                        field.isWestEnabled()
                                ));
                            }
                        }
                    }
                }

                listenerSet.stream().forEach(listener -> listener.onEvent(event));
                callbacks.forEach(c -> c.setEvent(event));
                callbacks.clear();

                break;
            default: // not interested in this event
        }
    }

    @Override
    public void setup(List<BoardModule> modules, boolean colorize) {

        this.boardModules = new ArrayList<>(modules);

        // sort alphabetically to ensure the order is always the same order so that a second setup does not force the player to change the
        // order of the physical modules
        boardModules.sort(Comparator.comparing(BoardModule::getId));

        // depending on the number of boards the layout is set.
        switch (boardModules.size()) {
            case 2:
                this.moduleMatrix = new BoardModule[2][1];
                this.moduleMatrix[0][0] = boardModules.get(0);
                this.moduleMatrix[1][0] = boardModules.get(1);
                break;
            default:
                this.moduleMatrix = null; // in case we call the method twice and the second attempt fails
                new IllegalStateException("[" + boardModules.size() + "] boards are not supported by the setup");
        }

        this.moduleSize = this.moduleMatrix[0][0].getSize();
        this.gameBoardSize = new Size(this.moduleMatrix.length * moduleSize.columns(), this.moduleMatrix[0].length * moduleSize.rows());

    }

    /**
     * Checks if the {@link #setup(List, boolean)} method was called successfully
     *
     * @return {@code true} if the board was initialized, {@code false} otherwise.
     */
    private boolean isInitialized() {
        return moduleMatrix != null;
    }

    @Override
    public Size getSize() {
        return gameBoardSize;
    }

    /**
     * Resolves the position on the game board to the underlying ühysical modules.
     *
     * @param position the position
     *
     * @return the resolved position
     */
    private ModulePosition resolve(Position position) {

        LOG.trace("Try to resolve field for position ::= [{}]", position);

        // Example: if we have a 3x4 matrix of board (of 6 LEDS per row each) the complete board has a size of 18x24.
        // If the row on the board of the tile is 9 the division through the number of LEDs per module will result
        // in the row for the target physical board module. We have to reduce the number by one to compensate the floor operation
        int moduleColumn = Math.floorDiv((position.column()), moduleSize.columns());
        int moduleRow = Math.floorDiv((position.row()), moduleSize.rows());

        LOG.trace("Resolved module column ::= [{}], module row ::= [{}]", moduleColumn, moduleRow);

        if (moduleColumn > moduleMatrix.length || moduleColumn < 0) {
            throw new IllegalArgumentException("The column value is not within the zero based space of the logical board:" +
                                                       " [" + moduleMatrix.length * moduleSize.columns() + "][" + moduleMatrix[0].length * moduleSize.rows() + "]");
        }

        if (moduleRow > moduleMatrix[0].length || moduleRow < 0) {
            throw new IllegalArgumentException("The column value is not within the zero based space of the logical board:" +
                                                       " [" + moduleMatrix.length * moduleSize.columns() + "][" + moduleMatrix[0].length * moduleSize.rows() + "]");
        }

        var boardModule = moduleMatrix[moduleColumn][moduleRow];


        // next step is to transform the values to the coordinates of the board module via a modulo operation
        int rowModule = position.row() % moduleSize.rows();
        int columnModule = position.column() % moduleSize.columns();

        LOG.trace("Resolved column of module ::= [{}], row of module ::= [{}]", columnModule, rowModule);

        return new ModulePosition(boardModule, new Position(columnModule, rowModule));
    }

    @Override
    public BoardField getField(Position position) {
        return resolve(position).getField();
    }

    @Override
    public void clearColors() {
        Arrays.stream(moduleMatrix)                      // all columns, i.e. horizontal modules
                .flatMap(m -> Arrays.stream(m))     // all rows, i.e. vertical modules
                .forEach(m -> m.clearColors());       // clear
    }

    @Override
    public String getId() {
        return "GameBoard";
    }

    @Override
    public void setColor(Position position, Color color) {
        resolve(position).setColor(color);
    }

    @Override
    public void sendColorUpdate(boolean clear) {
        Arrays.stream(moduleMatrix)                               // all columns, i.e. horizontal modules
                .flatMap(m -> Arrays.stream(m))              // all rows, i.e. vertical modules
                .forEach(m -> m.sendColorUpdate(clear));       // clear
    }
}

package mro.fantasy.game.devices.board.impl;

import mro.fantasy.game.Position;
import mro.fantasy.game.Size;
import mro.fantasy.game.devices.board.BoardField;
import mro.fantasy.game.devices.board.BoardModule;
import mro.fantasy.game.devices.events.DeviceEventHandler;
import mro.fantasy.game.devices.events.DeviceMessage;
import mro.fantasy.game.devices.impl.AbstractDevice;
import mro.fantasy.game.devices.impl.Color;
import mro.fantasy.game.devices.impl.DeviceType;
import mro.fantasy.game.devices.impl.ServerMessageType;
import mro.fantasy.game.engine.events.BoardUpdatedEvent;
import mro.fantasy.game.engine.events.GameEventListener;
import mro.fantasy.game.engine.events.impl.BoardUpdatedEventImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

/**
 * Implementation of a single physical board module that is managed by the {@link mro.fantasy.game.devices.board.BoardModule}
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-19
 */
public class BoardModuleImpl extends AbstractDevice<BoardUpdatedEvent, GameEventListener<BoardUpdatedEvent>> implements BoardModule, DeviceEventHandler {

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(BoardModuleImpl.class);

    /**
     * The number of rows and columns of the board module.
     */
    private static final Size BOARD_SIZE = new Size(8, 8);

    /**
     * Array of board fields for this module.
     */
    private final BoardFieldImpl[][] fields = new BoardFieldImpl[BOARD_SIZE.columns()][BOARD_SIZE.rows()];

    /**
     * A map that contains all colors for the LEDs on the board module to be updated
     */
    private final Map<Position, Color> colorsToUpdate = new HashMap<>();

    /**
     * Creates a new device.
     *
     * @param deviceId      the unique ID / name of this device.
     * @param deviceAddress the IP address of the device.
     * @param udpPort       the UDP port of the device.
     *
     * @throws IOException in case the UDP datagramm socket cannot be created
     */
    public BoardModuleImpl(String deviceId, InetAddress deviceAddress, int udpPort) throws IOException {
        super(deviceId, DeviceType.BOARD_MODULE, deviceAddress, udpPort);

        for (int column = 0; column < BOARD_SIZE.columns(); column++) {
            for (int row = 0; row < BOARD_SIZE.rows(); row++) {
                fields[column][row] = new BoardFieldImpl(deviceId, new Position(column, row));
            }
        }
        LOG.debug("Created new board module ::= [{}]", this);
    }

    @Override
    public void handle(DeviceMessage eventData) {
        if (eventData.getDeviceId().equals(deviceId)) {
            LOG.debug("[{}] - received device event ::= [{}]", deviceId, eventData.getEventType());

            final List<BoardField> changedFields = new ArrayList<>();

            switch (eventData.getEventType()) {
                case BOARD_BUTTON_PRESSED -> {
                    checkButtonSubModule(new Position(0, 0), eventData.getData(0), eventData.getData(1), changedFields); // Module A
                    checkButtonSubModule(new Position(4, 0), eventData.getData(2), eventData.getData(3), changedFields); // Module B
                    checkButtonSubModule(new Position(0, 4), eventData.getData(4), eventData.getData(5), changedFields); // Module C
                    checkButtonSubModule(new Position(4, 4), eventData.getData(6), eventData.getData(7), changedFields); // Module D
                }
                case BOARD_BOARD_CHANGED -> {
                    checkBoardSubModule(new Position(0, 0), eventData.getData(0), eventData.getData(1), changedFields); // Module A
                    checkBoardSubModule(new Position(4, 0), eventData.getData(2), eventData.getData(3), changedFields); // Module B
                    checkBoardSubModule(new Position(0, 4), eventData.getData(4), eventData.getData(5), changedFields); // Module C
                    checkBoardSubModule(new Position(4, 4), eventData.getData(6), eventData.getData(7), changedFields); // Module D
                }
                case BOARD_EDGE_CHANGED -> {
                    checkEdgeSubModule(new Position(0, 0), eventData.getData(), 0, changedFields); // Module A
                    checkEdgeSubModule(new Position(4, 0), eventData.getData(), 8, changedFields); // Module B
                    checkEdgeSubModule(new Position(0, 4), eventData.getData(), 16, changedFields); // Module C
                    checkEdgeSubModule(new Position(4, 4), eventData.getData(), 24, changedFields); // Module D
                }
            }

            LOG.debug("Changed state of ::= [{}] fields", changedFields.size());
            if (!changedFields.isEmpty()) broadcastEvent(new BoardUpdatedEventImpl(changedFields));  // inform all listener about the changed fields


        }
    }

    /**
     * Method to check for the board sensors of a submodule.The method expects the two bytes with the button data.
     *
     * @param offset        the offset column and row for the sector which is checked
     * @param dataHigh      the higher byte with the data
     * @param dataLow       the lower byte with the data
     * @param changedFields the list where the field is added in case a change was detected
     */
    private void checkBoardSubModule(Position offset, byte dataLow, byte dataHigh, List<BoardField> changedFields) {
        check(fields[offset.column()][offset.row()], BoardField.SensorType.Board, dataLow, 0, changedFields);
        check(fields[offset.column() + 1][offset.row()], BoardField.SensorType.Board, dataLow, 1, changedFields);
        check(fields[offset.column()][offset.row() + 1], BoardField.SensorType.Board, dataLow, 2, changedFields);
        check(fields[offset.column() + 1][offset.row() + 1], BoardField.SensorType.Board, dataLow, 3, changedFields);
        check(fields[offset.column() + 2][offset.row()], BoardField.SensorType.Board, dataLow, 4, changedFields);
        check(fields[offset.column() + 3][offset.row()], BoardField.SensorType.Board, dataLow, 5, changedFields);
        check(fields[offset.column() + 2][offset.row() + 1], BoardField.SensorType.Board, dataLow, 6, changedFields);
        check(fields[offset.column() + 3][offset.row() + 1], BoardField.SensorType.Board, dataLow, 7, changedFields);

        check(fields[offset.column() + 2][offset.row() + 2], BoardField.SensorType.Board, dataHigh, 0, changedFields);
        check(fields[offset.column() + 3][offset.row() + 2], BoardField.SensorType.Board, dataHigh, 1, changedFields);
        check(fields[offset.column() + 2][offset.row() + 3], BoardField.SensorType.Board, dataHigh, 2, changedFields);
        check(fields[offset.column() + 3][offset.row() + 3], BoardField.SensorType.Board, dataHigh, 3, changedFields);
        check(fields[offset.column()][offset.row() + 2], BoardField.SensorType.Board, dataHigh, 4, changedFields);
        check(fields[offset.column() + 1][offset.row() + 2], BoardField.SensorType.Board, dataHigh, 5, changedFields);
        check(fields[offset.column()][offset.row() + 3], BoardField.SensorType.Board, dataHigh, 6, changedFields);
        check(fields[offset.column() + 1][offset.row() + 3], BoardField.SensorType.Board, dataHigh, 7, changedFields);
    }

    /**
     * Method to check for the button sensors of a submodule.The method expects the two bytes with the button data
     *
     * @param offset        the offset column and row for the submodule which is checked
     * @param dataHigh      the higher byte with the data
     * @param dataLow       the lower byte with the data
     * @param changedFields the list where the field is added in case a change was detected
     */
    private void checkButtonSubModule(Position offset, byte dataLow, byte dataHigh, List<BoardField> changedFields) {
        check(fields[offset.column()][offset.row()], BoardField.SensorType.Button, dataLow, 0, changedFields);
        check(fields[offset.column() + 1][offset.row()], BoardField.SensorType.Button, dataLow, 1, changedFields);
        check(fields[offset.column()][offset.row() + 1], BoardField.SensorType.Button, dataLow, 2, changedFields);
        check(fields[offset.column() + 1][offset.row() + 1], BoardField.SensorType.Button, dataLow, 3, changedFields);
        check(fields[offset.column() + 2][offset.row()], BoardField.SensorType.Button, dataLow, 4, changedFields);
        check(fields[offset.column() + 3][offset.row()], BoardField.SensorType.Button, dataLow, 5, changedFields);
        check(fields[offset.column() + 2][offset.row() + 1], BoardField.SensorType.Button, dataLow, 6, changedFields);
        check(fields[offset.column() + 3][offset.row() + 1], BoardField.SensorType.Button, dataLow, 7, changedFields);

        check(fields[offset.column()][offset.row() + 2], BoardField.SensorType.Button, dataHigh, 0, changedFields);
        check(fields[offset.column() + 1][offset.row() + 2], BoardField.SensorType.Button, dataHigh, 1, changedFields);
        check(fields[offset.column()][offset.row() + 3], BoardField.SensorType.Button, dataHigh, 2, changedFields);
        check(fields[offset.column() + 1][offset.row() + 3], BoardField.SensorType.Button, dataHigh, 3, changedFields);
        check(fields[offset.column() + 2][offset.row() + 2], BoardField.SensorType.Button, dataHigh, 4, changedFields);
        check(fields[offset.column() + 3][offset.row() + 2], BoardField.SensorType.Button, dataHigh, 5, changedFields);
        check(fields[offset.column() + 2][offset.row() + 3], BoardField.SensorType.Button, dataHigh, 6, changedFields);
        check(fields[offset.column() + 3][offset.row() + 3], BoardField.SensorType.Button, dataHigh, 7, changedFields);
    }

    /**
     * Method to check changes in the edge sensors of a submodule. The whole UDP data array is passed together with the start and end index in the byte array to use for the sensors
     * of the submodule
     *
     * @param offset        the offset column and row for the submodule which is checked
     * @param data          the UDP data message
     * @param idxStart      the start index in the data array for the submodule with the passed offset
     * @param changedFields the list where the field is added in case a change was detected
     */
    private void checkEdgeSubModule(Position offset, byte[] data, int idxStart, List<BoardField> changedFields) {
        checkEdgeField(fields[offset.column()][offset.row()], data[idxStart], changedFields);
        checkEdgeField(fields[offset.column() + 1][offset.row()], (byte) (data[idxStart] >> 4), changedFields);
        checkEdgeField(fields[offset.column()][offset.row() + 1], data[idxStart + 1], changedFields);
        checkEdgeField(fields[offset.column() + 1][offset.row() + 1], (byte) (data[idxStart + 1] >> 4), changedFields);
        checkEdgeField(fields[offset.column() + 2][offset.row()], data[idxStart + 2], changedFields);
        checkEdgeField(fields[offset.column() + 3][offset.row()], (byte) (data[idxStart + 2] >> 4), changedFields);
        checkEdgeField(fields[offset.column() + 2][offset.row() + 1], data[idxStart + 3], changedFields);
        checkEdgeField(fields[offset.column() + 3][offset.row() + 1], (byte) (data[idxStart + 3] >> 4), changedFields);
        checkEdgeField(fields[offset.column()][offset.row() + 2], data[idxStart + 4], changedFields);
        checkEdgeField(fields[offset.column() + 1][offset.row() + 2], (byte) (data[idxStart + 4] >> 4), changedFields);
        checkEdgeField(fields[offset.column()][offset.row() + 3], data[idxStart + 5], changedFields);
        checkEdgeField(fields[offset.column() + 1][offset.row() + 3], (byte) (data[idxStart + 5] >> 4), changedFields);
        checkEdgeField(fields[offset.column() + 2][offset.row() + 2], data[idxStart + 6], changedFields);
        checkEdgeField(fields[offset.column() + 3][offset.row() + 2], (byte) (data[idxStart + 6] >> 4), changedFields);
        checkEdgeField(fields[offset.column() + 2][offset.row() + 3], data[idxStart + 7], changedFields);
        checkEdgeField(fields[offset.column() + 3][offset.row() + 3], (byte) (data[idxStart + 7] >> 4), changedFields);
    }

    /**
     * Method to check for the edge sensors of a single field. Only the first 4 bits of the byte are checked, i.e. a bit shift is necessary for some fields. Internally the {@link
     * #check(BoardFieldImpl, BoardField.SensorType, byte, int, List)} method is used
     *
     * @param field         the field to check and update
     * @param data          the data byte, shifted so that the first 4 bits match the 4 edge sensor values
     * @param changedFields the list where the field is added in case a change was detected
     */
    private void checkEdgeField(BoardFieldImpl field, byte data, List<BoardField> changedFields) {
        check(field, BoardField.SensorType.North, data, 0, changedFields);
        check(field, BoardField.SensorType.East, data, 1, changedFields);
        check(field, BoardField.SensorType.South, data, 2, changedFields);
        check(field, BoardField.SensorType.West, data, 3, changedFields);
    }

    /**
     * Checks if the state of the passed sensor type for the given field has changed. In case it was changed, the field is added to the list from the arguments.
     * <p>
     * A possible change is detected through the passed byte and position. If the bit at the passed position is 1 the sensor is considered active, i.ee. a magnetic field was
     * detected (or in case of a button pressed).
     *
     * @param field         the field to check and update
     * @param type          the sensor type
     * @param b             the byte from the UDP message with the data of the electronic board module
     * @param position      the position of the bit to check in the byte
     * @param changedFields the list where the field is added in case a change was detected
     */
    private void check(BoardFieldImpl field, BoardField.SensorType type, byte b, int position, List<BoardField> changedFields) {
        boolean currentState = field.isSensorEnabled(type);
        boolean newState = isSet(b, position);

        if (newState != currentState) {
            changedFields.add(field);
        }

        field.setSensorEnabled(type, newState);
    }

    /**
     * Checks if the bit at the given position is set to 1 or 0.
     *
     * @param b        the byte to check
     * @param position the position of the bit within the byte. If less than 0 it is set to zero, if larger than 7 it is set to 7
     *
     * @return {@code true} if the bit was set, {@code false} otherwise
     */
    private boolean isSet(byte b, int position) {
        int pos = position;

        if (pos < 0) pos = 0;         // correct the position so that we only have
        else if (pos > 7) pos = 7;    // valid values for the bit to check

        int mask = 1 << pos;         // create a bitmask to check the specific postion
        int x = b & mask;            // perform an AND operation so that the result x is the 2^x value at the given position if there was a 1 in b at that position

        // Example:
        // mask      ::= 0000 1000
        // b         ::= 1001 1000        (which would mean that the user has pressed 3 Buttons in the responsible sector on the board)
        // b & mask  ::= 0000 1000        which correspond to the value 8
        //
        // if the bit would not have been set, the result of the operation would be 0, i.e. every positive result means, that the bit at the given
        // position was set

        return x > 0;
    }

    @Override
    public Size getSize() {
        return BOARD_SIZE;
    }

    @Override
    public BoardField getField(Position position) {
        return fields[position.column()][position.row()];
    }


    @Override
    public synchronized void sendClearColors() {
        this.colorsToUpdate.clear();
        Arrays.stream(fields).flatMap(Arrays::stream).forEach(f -> f.setColor(Color.Black));
        sendData(ServerMessageType.BOARD_COLOR_CLEAR);
    }

    @Override
    public synchronized void setColor(Position position, Color color) {
        LOG.debug("[{}] - set position ::= [{}] to ::= [{}]", deviceId, position, color);
        fields[position.column()][position.row()].setColor(color);
        this.colorsToUpdate.put(position, color);
    }

    @Override
    public synchronized void sendColorUpdate() {

        LOG.debug("[{}] - Send color update to ::= [{}:{}]", this.deviceId, deviceAddress, deviceUDPPort);

        if (colorsToUpdate.isEmpty()) {
            LOG.debug("[{}] - no colors were changed, do not send an update", this.deviceId);
            return;
        }

        byte[] data = new byte[1 + colorsToUpdate.size() * 2];

        data[0] = (byte) colorsToUpdate.size();                                                              // counter of LEDs which are changed

        int i = 0;
        for (Map.Entry<Position, Color> entry : colorsToUpdate.entrySet()) {
            data[1 + 2 * i] = (byte) (entry.getKey().row() * BOARD_SIZE.columns() + entry.getKey().column()); // Calculate the pixel ID to set
            data[2 + 2 * i] = (byte) entry.getValue().getID();                                                // the used color from the predefined color type
            i++;
        }

        sendData(ServerMessageType.BOARD_COLOR_UPDATE, data);

    }

    @Override
    public void sendEnableSensors(boolean button, boolean board, boolean edge) {
        LOG.debug("[{}] - Send sensor event button ::= [{}], board ::= [{}], edge ::= [{}] to ::= [{}:{}]", this.deviceId, button, board, edge, deviceAddress, deviceUDPPort);
        byte[] data = new byte[1];

        if (edge) {
            data[0] |= 1;
        }

        if (button) {
            data[0] |= 1 << 1;
        } else {
            data[0] &= ~(1 << 1);
        }

        if (board) {
            data[0] |= 1 << 2;
        } else {
            data[0] &= ~(1 << 2);
        }

        sendData(ServerMessageType.BOARD_ENABLE_SENSOR, data);

    }

    @Override
    public void sendSetBrightness(int brightness) {
        byte b;
        if (brightness < 0) {
            b = 0;
        } else if (brightness > 100) {
            b = 100;
        } else {
            b = (byte) brightness;
        }

        sendData(ServerMessageType.BOARD_SET_BRIGHTNESS, new byte[]{(byte) (b * 255 / 100)});

    }

    @Override
    public String toString() {
        return "BoardModuleImpl{" +
                       "deviceAddress=" + deviceAddress +
                       ", deviceUDPPort=" + deviceUDPPort +
                       ", deviceType=" + deviceType +
                       ", deviceId='" + deviceId + '\'' +
                       '}';
    }
}

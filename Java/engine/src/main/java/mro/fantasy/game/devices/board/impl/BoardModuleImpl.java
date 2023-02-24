package mro.fantasy.game.devices.board.impl;

import mro.fantasy.game.Position;
import mro.fantasy.game.Size;
import mro.fantasy.game.devices.board.BoardModule;
import mro.fantasy.game.devices.events.DeviceMessage;
import mro.fantasy.game.devices.events.DeviceMessageType;
import mro.fantasy.game.devices.impl.AbstractDevice;
import mro.fantasy.game.devices.impl.Color;
import mro.fantasy.game.devices.impl.DeviceType;
import mro.fantasy.game.devices.impl.ServerMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of a single physical board module that is managed by the {@link mro.fantasy.game.devices.board.BoardModule}
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-19
 */
public class BoardModuleImpl extends AbstractDevice implements BoardModule {

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(BoardModuleImpl.class);

    /**
     * The number of rows and columns of the board module.
     */
    private static final Size BOARD_SIZE = new Size(4, 4);

    /**
     * Array of board fields for this module.
     */
    private BoardFieldImpl[][] fields = new BoardFieldImpl[BOARD_SIZE.columns()][BOARD_SIZE.rows()];

    /**
     * A map that contains all colors for the LEDs on the board module to be updated
     */
    private Map<Position, Color> colorsToUpdate = new HashMap<>();

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
                fields[column][row] = new BoardFieldImpl(new Position(column, row));
            }
        }
        LOG.debug("Created new board module ::= [{}]", this);;

    }

    @Override
    public Size getSize() {
        return BOARD_SIZE;
    }

    @Override
    public BoardFieldImpl getField(Position position) {
        return fields[position.column()][position.row()];
    }


    @Override
    public synchronized void clearColors() {
        this.colorsToUpdate.clear();
        Arrays.stream(fields).flatMap(columns -> Arrays.stream(columns)).forEach(f -> f.setColor(Color.OFF));
        sendData(ServerMessageType.BOARD_COLOR_CLEAR);
    }

    @Override
    public synchronized void setColor(Position position, Color color) {
        LOG.debug("[{}] - set position ::= [{}] to ::= [{}]", deviceId, position, color);
        fields[position.column()][position.row()].setColor(color);
        this.colorsToUpdate.put(position, color);
    }

    @Override
    public synchronized void sendColorUpdate(boolean clear) {

        LOG.debug("[{}] - Send color update to ::= [{}:{}]", this.deviceId, deviceAddress, deviceUDPPort);

        if (colorsToUpdate.isEmpty()) {
            LOG.debug("[{}] - no colors were changed, do not send an update", this.deviceId);
            return;
        }

        ByteBuffer buf = ByteBuffer.allocate(512);
        buf.put((byte) colorsToUpdate.size()); // counter of LEDs which are changed

        colorsToUpdate.forEach((position, color) -> {
            buf.put((byte) position.column());
            buf.put((byte) position.row());
            buf.put((byte) color.red());
            buf.put((byte) color.green());
            buf.put((byte) color.blue());
        });

        buf.position(0);                                                  // reset the current position of the byte buffer to the first byte
        byte[] byteData = new byte[1 + colorsToUpdate.size() * 5];        // create a new array to send it to the Arduino device
        buf.get(byteData, 0, 1 + colorsToUpdate.size() * 5);  // copy the content of the byte buffer

        // DeviceMessage ddp = new DeviceMessage(DeviceType.SERVER, "SERVER",
        //         clear ? DeviceMessageType.BOARD_COLOR_CLEAR_AND_UPDATE.getEventId() : DeviceMessageType.BOARD_COLOR_UPDATE.getEventId(),
        //         byteData);
        //
        // try {
        //     DatagramPacket datagramPacket = new DatagramPacket(ddp.getRaw(), ddp.getRaw().length, deviceAddress, deviceUDPPort);
        //     socket.send(datagramPacket);
        //     LOG.debug("[{}] - Send datagram ::= [{}]", this.deviceId, ddp);
        //     this.colorsToUpdate.clear();
        // } catch (IOException e) {
        //     LOG.warn("[{}] - Could not send data package ::= [{}]:", this.deviceId, ddp, e);
        // }
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

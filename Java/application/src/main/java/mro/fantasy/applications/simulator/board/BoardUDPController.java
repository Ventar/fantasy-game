package mro.fantasy.applications.simulator.board;

import mro.fantasy.game.devices.events.DeviceMessage;
import mro.fantasy.game.devices.events.DeviceEventHandler;
import mro.fantasy.game.devices.events.DeviceMessageType;
import mro.fantasy.game.devices.events.impl.UDPDeviceEventServiceImpl;
import mro.fantasy.game.devices.impl.DeviceType;
import mro.fantasy.game.utils.ServiceThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.DatagramSocket;
import java.util.List;

/**
 * Controller Instance responsible for the UDP handling. The controller will handle incoming events from the server which are primary updates of the LEDs on the board. On the other
 * hand it will send the updated HAL sensor information to the server on a regular base. This reflects the microcontroller based implementation of the real boards which send the
 * information in that way.
 *
 * @author Michael Rodenbuecher
 * @since 2022-11-21
 */
@Component
public class BoardUDPController extends ServiceThread implements DeviceEventHandler {

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(BoardUDPController.class);

    /**
     * The type of the device
     */
    private DeviceType deviceType = DeviceType.BOARD_MODULE;

    /**
     * The device ID. For real devices this is the MAC address.
     */
    @Value("${game.device.id}")
    private String deviceId;

    /**
     * The data model of the board.
     */
    @Autowired
    private BoardModel model;

    /**
     * The board frame.
     */
    @Autowired
    private BoardFrame frame;

    /**
     * The last time an update about the sensor state was send to the server
     */
    private long lastSend;

    /**
     * The port to send data to. This is determined by the MDNS Protocol.
     */
    private int serverPort;

    /**
     * The address of the game server. This is determined by the MDNS Protocol.
     */
    private String serverAddress;

    /**
     * Socket to send out data to the server via UDP
     */
    private DatagramSocket socket;

    /**
     * Creates a new controller.
     *
     * @throws IllegalStateException if the underlying socket could not be created
     */
    public BoardUDPController() {
        try {
            this.socket = new DatagramSocket();
            this.setName("UDP");
            setLogger(LOG);
            setSleep(1000);
            lastSend = System.currentTimeMillis(); // that is fine, we do not need to send anything before we started :)
            this.start();
        } catch (Exception e) {
            throw new IllegalStateException("Could not create a datagram socket: ", e);
        }
    }


    @Override
    public void handle(DeviceMessage eventData) {

        byte[] data = eventData.getData();

        switch (DeviceMessageType.fromID(eventData.getEventId())) {
            case REGISTER:
                this.serverAddress = Byte.toUnsignedInt(data[0]) + "." + Byte.toUnsignedInt(data[1]) + "." + Byte.toUnsignedInt(data[2]) + "." + Byte.toUnsignedInt(data[3]);
                this.serverPort = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);
                LOG.debug("Set server UDP address to ::= [{}:{}]", this.serverAddress, this.serverPort);
                break;
            case BOARD_COLOR_CLEAR:
                model.clearColors();
                break;
            case BOARD_COLOR_CLEAR_AND_UPDATE:
                model.clearColors();
            case BOARD_COLOR_UPDATE:

                int column, row, red, green, blue, ledCount = 0;

                ledCount = Byte.toUnsignedInt(data[0]);
                LOG.trace("Try to update ::= [{}] leds", ledCount);

                for (int led = 0; led < ledCount; led++) {
                    column = Byte.toUnsignedInt(data[1 + led * 5]);
                    row = Byte.toUnsignedInt(data[2 + led * 5]);
                    red = Byte.toUnsignedInt(data[3 + led * 5]);
                    green = Byte.toUnsignedInt(data[4 + led * 5]);
                    blue = Byte.toUnsignedInt(data[5 + led * 5]);

                    // The swing frame has the 0|0 field in the upper left but the server is based on the lower left corner.

                    model.setColor(column, Configuration.ROWS - row - 1, new Color(red, green, blue));
                    LOG.trace("Set color of led ({}|{}) to ({},{},{})", column, row, red, green, blue);

                }

                break;
        }

        frame.repaint();

    }

    @Override
    public void work() {

        if (serverAddress == null || serverPort == 0) {  // REGISTER event was not received yet
            return;
        }

        List<BoardField> changedFields = model.getChangedFields(lastSend);

        if (!changedFields.isEmpty()) {
            LOG.debug("Found changed board fields ::= {}", changedFields);
            byte[] data = new byte[1 + changedFields.size() * 3];

            data[0] = (byte) changedFields.size();

            for (int f = 0; f < changedFields.size(); f++) {
                BoardField boardField = changedFields.get(f);
                data[1 + 3 * f] = (byte) boardField.getColumn();
                data[2 + 3 * f] = (byte) boardField.getRow();
                data[3 + 3 * f] = boardField.getSensorState();
            }

            sendData(DeviceMessageType.BOARD_SENSOR_UPDATE, data);

        }

        this.lastSend = System.currentTimeMillis();
    }

    /**
     * Constructs the header of an outgoing event and append the passed data. Afterwards this is sent as a datagram packet to the server via UDP.
     *
     * @param event the id of the event that defines the data that is sent.
     * @param data  the event data
     *
     * @see UDPDeviceEventServiceImpl
     * @see DeviceMessage
     */
    public void sendData(DeviceMessageType event, byte[] data) {
        // DeviceDataPackage dataPackage = new DeviceDataPackage(deviceType, deviceId, event.getEventId(), data);
        //
        // LOG.debug("Try to send data package ::= [{}]", dataPackage);
        //
        // try {
        //     DatagramPacket datagramPacket = new DatagramPacket(dataPackage.getRaw(), dataPackage.getRaw().length, InetAddress.getByName(serverAddress), serverPort);
        //     socket.send(datagramPacket);
        // } catch (IOException e) {
        //     LOG.warn("Could not send data package ::= [{}]:", dataPackage, e);
        // }

    }


}

package mro.fantasy.game.devices.events;

import mro.fantasy.game.devices.impl.AbstractMessage;
import mro.fantasy.game.devices.impl.DeviceType;
import mro.fantasy.game.utils.Condition;
import mro.fantasy.game.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Class to parse the basic part of UDP packages which were sent to the game server as an event from one of the connected devices. Every package send to the server follows the
 * following structure:
 *
 * <pre>{@code
 * part -  | HEADER                               |  DATA
 * byte -  |  0  - 5      | 6           | 7       |  [8+]
 * data -  | device ID    | device Type | eventId |  [data]
 * } </pre>
 * <p>
 * The device ID is used to identify the device uniquely within the game system, the {@link DeviceType} is used to distribute the event to the correct handler and allows in
 * combination with the {@code eventId} the parsing of the data that is following the event ID.
 * <p>
 * This header is the same for every device that is part of the game, i.e. the parsing of it can be done in this class. The parsing of the data depends on the header information
 * and must be done in the device specific handler.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-15
 */
public class DeviceMessage extends AbstractMessage {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DeviceMessage.class);

    /**
     * The raw datagram content.
     */
    private byte[] raw;

    /**
     * The type of device that sent the event.
     */
    private DeviceType deviceType;

    /**
     * The unique device ID which is represented by the MAC address of the device.
     */
    private String deviceId = "";

    /**
     * Creates a new instance from the passed UDP datagram package
     *
     * @param datagram the data from the event.
     *
     * @throws IllegalArgumentException if the passed datagram cannot be parsed
     */
    private DeviceMessage(byte[] datagram) {

        ValidationUtils.requireNonNull(datagram, "The datagram package cannot be null");
        ValidationUtils.requireFalse(datagram.length < 8, "The header field of the datagram package must contain at least 8 bytes.");

        // the next part can only retrieve the data, additional validation must be done by the event handler.

        Condition.of(datagram.length > 8).ifTrue(() -> this.data = Arrays.copyOfRange(datagram, 8, datagram.length));  // data

        this.deviceId = new String(Arrays.copyOfRange(datagram, 0, 6));

        this.deviceType = DeviceType.fromInteger(datagram[6]);                                                              // deviceType
        this.eventId = Byte.toUnsignedInt(datagram[7]);                                                                     // eventId
        this.raw = datagram;
        this.data = new byte[raw.length - 8];
        System.arraycopy(raw, 8, this.data, 0, raw.length - 8);                                         // data

        LOG.trace("[{}] - created device data package of size ::= [{}]", deviceId, raw.length);

    }

    /**
     * Creates a new message from the passed UDP datagram
     * @param datagram the raw data
     * @return the new message
     */
    public static DeviceMessage parse(byte[] datagram) {
        return new DeviceMessage(datagram);
    }
    
    // /**
    //  * Creates a data package from the given arguments.
    //  *
    //  * @param deviceType The bytes which represent the data part of the event UDP package
    //  * @param deviceId   The unique device ID which is represented by the MAC address of the device.
    //  * @param eventId    The ID of the event that is sent by the device. Every device can send different events where the following data in the byte stream depends on the event id
    //  *                   that was sent.
    //  * @param data       the actual data that is send after the header, can be {@code null}
    //  */
    // public DeviceDataPackage(DeviceType deviceType, String deviceId, int eventId, byte[] data) {
    //
    //     if (deviceId == null || deviceId.length() != 6) {
    //         throw new IllegalArgumentException("The device ID ::= [" + deviceId + "] has to have 6 letters");
    //     }
    //
    //     this.deviceType = deviceType;
    //     this.deviceId = deviceId;
    //     this.eventId = eventId;
    //
    //     var bytesDeviceId = this.deviceId.getBytes(StandardCharsets.UTF_8);
    //
    //     // if no data was set because the event is sufficient, we simply send a 0 to make the code on this side easier.
    //     this.data = data == null ? new byte[]{0} : data;
    //
    //     this.raw = new byte[8 + data.length];
    //     IntStream.range(0, 6).forEach(i -> this.raw[i] = bytesDeviceId[i] );           // device ID
    //     this.raw[6] = (byte) deviceType.getTypeId();                                    // deviceType
    //     this.raw[7] = (byte) eventId;                                                   // eventId
    //     System.arraycopy(data, 0, this.raw, 8, data.length);              // data
    //
    //     LOG.trace("[{}] - created device data package of size ::= [{}]", deviceId, raw.length);
    // }

    /**
     * The type of device that sent the event.
     *
     * @return the device type
     */
    public DeviceType getDeviceType() {
        return deviceType;
    }

    /**
     * Returns the unique device ID which is represented by the MAC address of the device.
     *
     * @return the device ID
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Converts the {@link #getEventId()} into the corresponding enum value
     *
     * @return the type
     */
    public DeviceMessageType getEventType() {
        return DeviceMessageType.fromID(getEventId());
    }

    /**
     * Returns the complete raw data of the datagram packet.
     *
     * @return the raw data
     */
    public byte[] getRaw() {
        return raw;
    }

    @Override
    public String toString() {
        return "DeviceDataPackage{" +
                       "deviceType=" + deviceType +
                       ", deviceId='" + deviceId + '\'' +
                       ", eventId=" + DeviceMessageType.fromID(eventId) +
                       '}';
    }
}

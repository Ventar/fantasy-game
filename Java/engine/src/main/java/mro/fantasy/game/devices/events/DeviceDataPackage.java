package mro.fantasy.game.devices.events;

import mro.fantasy.game.devices.DeviceType;
import mro.fantasy.game.utils.Condition;
import mro.fantasy.game.utils.ValidationUtils;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.stream.IntStream;

/**
 * Class to parse the basic part of UDP packages which were sent to the game server as an event from one of the connected devices. Every package send to the server follows the
 * following structure:
 *
 * <pre>{@code
 * part -  | HEADER                                           |  DATA
 * byte -  |  0   | 1    |     | 6    | 7           | 8       |  [9+]
 * data -  | device MAC Address       | device Type | eventId |  [data]
 * } </pre>
 * <p>
 * The MAC address is used to identify the device uniquely within the game system, the {@link DeviceType} is used to distribute the event to the correct handler and allows in
 * combination with the {@code eventId} the parsing of the data that is following the event ID.
 * <p>
 * This header is the same for every device that is part of the game, i.e. the parsing of it can be done in this class. The parsing of the data depends on the header information
 * and must be done in the device specific handler.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-15
 */
public class DeviceDataPackage {

    /**
     * The raw datagram content.
     */
    private byte[] raw;

    /**
     * The bytes which represent the data part of the device UDP package
     */
    private byte[] data;

    /**
     * The type of device that sent the event.
     */
    private DeviceType deviceType;

    /**
     * The unique device ID which is represented by the MAC address of the device.
     */
    private String deviceId = "";

    /**
     * The ID of the event that is sent by the device. Every device can send different events where the following data in the byte stream depends on the event id that was sent.
     */
    private int eventId;

    /**
     * Creates a new instance from the passed UDP datagram package
     *
     * @param datagram the data from the event.
     *
     * @throws IllegalArgumentException if the passed datagram cannot be parsed
     */
    public DeviceDataPackage(byte[] datagram) {

        ValidationUtils.requireNonNull(datagram, "The datagram package cannot be null");
        ValidationUtils.requireFalse(datagram.length < 8, "The header field of the datagram package must contain at least 8 bytes.");

        // the next part can only retrieve the data, additional validation must be done by the event handler.

        Condition.of(datagram.length > 8).ifTrue(() -> this.data = Arrays.copyOfRange(datagram, 9, datagram.length));  // data
        IntStream.range(0, 6).forEach(i -> this.deviceId += String.format("%02X", datagram[i]));                            // deviceId
        this.deviceType = DeviceType.fromInteger(datagram[7]);                                                              // deviceType
        this.eventId = datagram[8];                                                                                         // eventId
        this.raw = datagram;

    }

    /**
     * Creates a data package from the given arguments.
     *
     * @param deviceType The bytes which represent the data part of the event UDP package
     * @param deviceId   The unique device ID which is represented by the MAC address of the device.
     * @param eventId    The ID of the event that is sent by the device. Every device can send different events where the following data in the byte stream depends on the event id
     *                   that was sent.
     * @param data
     */
    public DeviceDataPackage(DeviceType deviceType, String deviceId, int eventId, byte[] data) {
        this.deviceType = deviceType;
        this.deviceId = deviceId;
        this.eventId = eventId;
        this.data = data;

        this.raw = new byte[8 + data.length];
        byte[] rawDeviceId = HexFormat.of().parseHex(deviceId);
        IntStream.range(0, 6).forEach(i -> raw[i] = rawDeviceId[i]);                    // device ID
        this.raw[7] = (byte) deviceType.getTypeId();                                    // deviceType
        this.raw[8] = (byte) eventId;                                                   // eventId
        System.arraycopy(data, 0, this.raw, 8, data.length);              // data
    }


    /**
     * Returns the bytes which represent the data part of the event UDP package
     *
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

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
     * Returns the ID of the event that is sent by the device. Every device can send different events where the following data in the byte stream depends on the event id that was
     * sent.
     *
     * @return the id
     */
    public int getEventId() {
        return eventId;
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
        return "DeviceEventDataPackage{" +
                       "deviceType=" + deviceType +
                       ", deviceId='" + deviceId + '\'' +
                       ", eventId=" + eventId +
                       '}';
    }
}

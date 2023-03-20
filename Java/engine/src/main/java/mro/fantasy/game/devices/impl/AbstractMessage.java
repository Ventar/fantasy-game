package mro.fantasy.game.devices.impl;

/**
 * Base class for data packages send between the game server and the devices.
 *
 * @author Michael Rodenbuecher
 * @since 2023-02-24
 */
public class AbstractMessage {

    /**
     * The ID of the event that is sent by the device. Every device can send different events where the following data in the byte stream depends on the event id that was sent.
     */
    protected int eventId;

    /**
     * The bytes which represent the data part of the device UDP package
     */
    protected byte[] data;

    /**
     * Returns the bytes which represent the data part of the event UDP package
     *
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns the byte ath the given position from the raw data array.
     *
     * @param idx the index
     * @return the data
     */
    public byte getData(int idx) {
        return data[idx];
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

    @Override
    public String toString() {
        String s = "AbstractMessage{" +
                           "eventId=" + eventId +
                           ", data=[";
        for (int i = 0; i < data.length; i++) {
            s += Byte.toUnsignedInt(data[i]);
            if (i < data.length - 1) {
                s += ", ";
            }
        }

        s += "]}";
        return s;
    }
}

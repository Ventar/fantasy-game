package mro.fantasy.game.devices.events;

/**
 * A handler that is capable of handling {@link DeviceDataPackage}s and process their content.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-13
 */
public interface DeviceEventHandler {

    /**
     * Handles the passed event data IF the handler is responsible for thw data. Based on the header information in the data package the handle should decide if it is responsible
     * for the event. Usually the {@link DeviceDataPackage#getDeviceType()} is used to determine the responsibility
     *
     * @param eventData the preparsed event data to handle.
     */
    void handle(DeviceDataPackage eventData);
}

package mro.fantasy.game.devices.board.impl;

import mro.fantasy.game.devices.DeviceType;
import mro.fantasy.game.devices.board.BoardModule;
import mro.fantasy.game.devices.discovery.impl.AbstractDevice;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Implementation of a single physical board module that is managed by the {@link mro.fantasy.game.devices.board.BoardController}
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-19
 */
public class BoardModuleImpl extends AbstractDevice implements BoardModule {

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
    }
}

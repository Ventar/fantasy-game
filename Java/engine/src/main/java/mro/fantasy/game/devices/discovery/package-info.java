package mro.fantasy.game.devices.discovery;

/**
 * This package is responsible for the device discovery of the various physical parts of the game system.
 * <p>
 * To detect devices the {@link mro.fantasy.game.devices.discovery.DeviceDiscoveryService} starts a MDNS listener that waits for registration messages from the game devices. While
 * the process is async the implementation uses a {@link java.util.concurrent.Future} to synchronize the discovery process. This makes it easier to perform the actual setup of the
 * various devices by the game engine.
 * <p>
 * As a result the discovery service will run a certain period of time when the {@link mro.fantasy.game.devices.discovery.DeviceDiscoveryService#scan()} method was called and
 * caches the results afterwards. The MDNS service is shutdown afterwards, i.e. it is not possible to add additional devices to the network afterwards. If that is needed the {@code
 * scan()} can be called again.
 */
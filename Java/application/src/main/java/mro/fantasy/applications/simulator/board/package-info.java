/**
 * Simulator for physical board modules.
 * <p>
 * The simulator allows developers to use virtual {@link mro.fantasy.game.devices.board.BoardModule}s instead of real physical hardware. It can be used to trigger HAL sensor
 * changes on the board module and display LED colors.
 *
 * <img src="doc-files/BoardSimulator.png"/>
 * <p>
 * The Implementation reuses Spring services from the core engine to handle UDP events send by the server. To achieve this the {@link mro.fantasy.applications.simulator.board.Configuration}
 * class instantiates Spring Beans of the {@link mro.fantasy.game.devices.events.DeviceEventService} that is used on the server to receive events from the devices.
 * <p>
 * Everything is build around the {@link mro.fantasy.applications.simulator.board.BoardDeviceSimulator} which is a regular Spring Boot application. It uses the {@link
 * mro.fantasy.applications.simulator.board.BoardModel} and the {@link mro.fantasy.applications.simulator.board.BoardFrame} to display a single board module in a Swing JFrame.
 * <p>
 * The developer can enable and disable the HAL sensors and see which LEDs are turned on based on the data send from the server. The communication itself is done via UDP, i.e. from
 * server perspective there is no difference between real hardware and the emulated software.
 * <p>
 * Sending events to the server is done in a similar way as on the Arduino based hardware, i.e. the state of the sensor is polled on a regular basis instead of triggering the
 * events when a sensor is selected.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-23
 */
package mro.fantasy.applications.simulator.board;
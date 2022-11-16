package mro.fantasy.applications.simulator.board;

import mro.fantasy.game.devices.DeviceType;
import mro.fantasy.game.devices.discovery.DeviceDiscoveryService;
import mro.fantasy.game.devices.events.DeviceDataPackage;
import mro.fantasy.game.devices.events.DeviceEventHandler;
import mro.fantasy.game.devices.events.impl.UDPDeviceEventServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Utility class to simulate a device.
 *
 * @author Michael Rodenbuecher
 * @see UDPDeviceEventServiceImpl
 * @since 2022-08-13
 */

@Configuration
@ComponentScan
public class BoardDeviceSimulator implements CommandLineRunner, DeviceEventHandler {

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(BoardDeviceSimulator.class);

    /**
     * The type of the device
     */
    private DeviceType deviceType = DeviceType.BOARD_MODULE;

    /**
     * UDP port that is used by the {@link UDPDeviceEventServiceImpl} to listen for incoming events.
     */
    @Value("${game.event.udp.port}")
    private int eventUDPPort;

    /**
     * UDP address that is used by the {@link UDPDeviceEventServiceImpl} to listen for incoming events.
     */
    @Value("${game.ip.address}")
    private String ipAddress;

    /**
     * Size of the datagram package that is used to read the UDP game events.
     */
    @Value("${game.event.udp.buffer.bytes}")
    private int eventUDPBufferBytes;

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
     * The port to send data to.
     */
    private int serverPort;

    /**
     * The address of the game server.
     */
    private String serverAddress;


    /**
     * Socket to send out data to the server via UDP
     */
    private DatagramSocket socket;


    /**
     * Creates a new simulator.
     *
     * @throws IllegalStateException if the underlying socket could not be created
     */
    public BoardDeviceSimulator() {
        try {
            this.socket = new DatagramSocket();
        } catch (Exception e) {
            throw new IllegalStateException("Could not create a datagram socket: ", e);
        }
    }

    /**
     * Constructs the header of an outgoing event and append the passed data. Afterwards this is send as a datagram packet to the server via UDP.
     *
     * @param eventId the id of the event that defines the data that is sent.
     * @param data    the event data
     *
     * @see UDPDeviceEventServiceImpl
     * @see DeviceDataPackage
     */
    public void sendData(int eventId, byte[] data) {
        DeviceDataPackage dataPackage = new DeviceDataPackage(deviceType, deviceId, eventId, data);

        LOG.debug("Try to send data package ::= [{}]", dataPackage);

        try {
            DatagramPacket datagramPacket = new DatagramPacket(dataPackage.getRaw(), dataPackage.getRaw().length, InetAddress.getByName(serverAddress), serverPort);
            socket.send(datagramPacket);
        } catch (IOException e) {
            LOG.warn("Could not send data package ::= [{}]:", dataPackage, e);
        }

    }

    @Override
    public void run(String... args) throws Exception {
        JmDNS jmdns = JmDNS.create(InetAddress.getByName(ipAddress));
        ServiceInfo serviceInfo = ServiceInfo.create(DeviceDiscoveryService.BOARD_MDNS_TYPE, deviceId, eventUDPPort, "n/a");
        jmdns.registerService(serviceInfo);

        while (true) {


            Thread.sleep(5000);
        }

    }

    @Override
    public void handle(DeviceDataPackage eventData) {
        LOG.debug("Received DeviceDataPackage ::= [{}]", eventData);

        byte[] data = eventData.getData();

        switch (eventData.getEventId()) {
            case 0: // DeviceEventType.REGISTER.getEventId()
                this.serverAddress = Byte.toUnsignedInt(data[0]) + "." + Byte.toUnsignedInt(data[1]) + "." + Byte.toUnsignedInt(data[2]) + "." + Byte.toUnsignedInt(data[3]);
                this.serverPort = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);
                break;
        }

    }

    /**
     * Runs the applicaton. As part of the argument list the UDP Port and the unique devive ID should be specified.
     *
     * --game.event.udp.port=4001 --game.device.id=b8eb077f9f7a
     *
     * @param args the passed arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(BoardDeviceSimulator.class, args);
    }


}

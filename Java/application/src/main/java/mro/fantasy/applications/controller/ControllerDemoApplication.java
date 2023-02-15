package mro.fantasy.applications.controller;

import mro.fantasy.game.Position;
import mro.fantasy.game.devices.board.GameBoard;
import mro.fantasy.game.devices.board.impl.BoardModuleRenderer;
import mro.fantasy.game.devices.discovery.impl.DeviceDiscoveryServiceImpl;
import mro.fantasy.game.devices.events.DeviceEventService;
import mro.fantasy.game.devices.impl.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;


@ComponentScan({"mro.fantasy.game", "mro.fantasy.applications.board"})
public class ControllerDemoApplication implements CommandLineRunner {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ControllerDemoApplication.class);

    //@Autowired
    //private GameLibrary library;

    // @Autowired
    // private PlanDeltaService deltaService;
    //@Autowired
    //private BoardController gameBoard;

    @Autowired
    private DeviceDiscoveryServiceImpl deviceDiscoveryService;

    @Autowired
    private DeviceEventService deviceEventService;

    @Autowired
    private GameBoard gameBoard;

    public static void main(String[] args) {
        SpringApplication.run(ControllerDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {


        LOG.debug("Wait for device discovery service");
        deviceDiscoveryService.scan();
        //deviceEventService.start();


        LOG.info("");
        LOG.info("---------------------------------------------------------------------------------");
        LOG.info("APPLICATION SUCCESSFULLY INITIALIZED");
        LOG.info("---------------------------------------------------------------------------------");
        LOG.info("");




    }
}

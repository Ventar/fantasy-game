package mro.fantasy.applications.board;

import mro.fantasy.game.Position;
import mro.fantasy.game.devices.board.BoardField;
import mro.fantasy.game.devices.board.BoardModule;
import mro.fantasy.game.devices.board.GameBoard;
import mro.fantasy.game.devices.discovery.impl.DeviceDiscoveryServiceImpl;
import mro.fantasy.game.devices.events.DeviceEventService;
import mro.fantasy.game.devices.impl.Color;
import mro.fantasy.game.engine.events.BoardUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;


@ComponentScan({"mro.fantasy.game", "mro.fantasy.applications.board"})
public class BoardDemoApplication implements CommandLineRunner {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(BoardDemoApplication.class);

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
        SpringApplication.run(BoardDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {


        LOG.debug("Wait for device discovery service");
        deviceDiscoveryService.scan();
        deviceEventService.start();
        //gameBoard.setup(deviceDiscoveryService.getBoardModules(), true);

        LOG.info("");
        LOG.info("---------------------------------------------------------------------------------");
        LOG.info("APPLICATION SUCCESSFULLY INITIALIZED");
        LOG.info("---------------------------------------------------------------------------------");
        LOG.info("");

        //gameBoard.setColor(new Position(3, 3), Color.BLUE);
        //gameBoard.sendColorUpdate(false);

        BoardModule mod = deviceDiscoveryService.getBoardModules().get(0);

        mod.sendClearColors();
        mod.setColor(new Position(0, 0), Color.DarkViolet);
        mod.sendColorUpdate();
        mod.sendEnableSensors(true, true, true);
        mod.sendSetBrightness(25);

        boolean resolved = false;

        do {
            BoardUpdatedEvent event = mod.waitForEvent().get(); // block execution until an event was received
            resolved = event.isSensorActive(BoardField.SensorType.Button, new Position(0, 0));
            LOG.debug("Handled event, resolved ::= [{}]", resolved);
        } while (!resolved);


        System.exit(0);

        //BoardModuleRenderer renderer = new BoardModuleRenderer();
        //gameBoard.registerListener(event -> renderer.render(gameBoard));


    }
}

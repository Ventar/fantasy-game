package mro.fantasy.game.engine;

import mro.fantasy.game.communication.AudioCommunicationService;
import mro.fantasy.game.plan.Plan;
import mro.fantasy.game.plan.PlanLibrary;
import mro.fantasy.game.plan.TileLibrary;
import mro.fantasy.game.plan.TileTemplate;
import mro.fantasy.game.plan.impl.PlanImpl;
import mro.fantasy.game.plan.impl.TileTemplateImpl;
import mro.fantasy.game.resources.GameResource;
import mro.fantasy.game.resources.ResourceLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.function.Function;

/**
 * The overall library that provides {@link GameResource}s. This service is used to aggregate all game resources in a single place to make access to the data easy.
 */
@Service
public class GameLibrary {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(GameLibrary.class);

    /**
     * The state of the game library.
     */
    private enum State {
        /**
         * The library was created.
         */
        CREATED,
        /**
         * The {@link #loadResources()} method was called and is currently initializing the library
         */
        INITIALIZING,
        /**
         * The library was fully initalized.
         */
        READY
    }

    /**
     * Indicator if the {@link #loadResources()} method of this class was executed once. If not an exception is thrown.
     */
    private State state;

    /**
     * The tile library.
     *
     * @see #getTileLibrary()
     */
    @Autowired
    private TileLibrary tileLibrary;

    /**
     * The plan library.
     *
     * @see #getPlanLibrary()
     */
    @Autowired
    private PlanLibrary planLibrary;

    /**
     * The audio communication is a special type of {@link ResourceLibrary} that offers more service functionality than the other libraries (play audio files) but it has to be
     * initialized in a similar way and was added to this library for that reason
     */
    @Autowired
    private AudioCommunicationService audioCommunicationService;

    /**
     * Collection of all resource libraries known by the game engine.
     */
    @Autowired
    private Collection<ResourceLibrary> libraries;

    /**
     * Returns the library with the {@link TileTemplate}s to create a {@link Plan}.
     *
     * @return the library
     */
    public TileLibrary getTileLibrary() {
        checkInitialization();
        return tileLibrary;
    }

    /**
     * Returns the library with all {@link Plan}s known by the current game installation
     *
     * @return the library
     */
    public PlanLibrary getPlanLibrary() {
        checkInitialization();
        return planLibrary;
    }

    /**
     * Calls the {@link ResourceLibrary#loadResources(Function)} method for every library to load all needed data. Some of the libraries depend on each other so that this method is
     * responsible for the order.
     */
    public void loadResources() {
        state = State.INITIALIZING;

        LOG.info("");
        LOG.info("---------------------------------------------------------------------------------");
        LOG.info("INITIALIZE GAME LIBRARY");
        LOG.info("---------------------------------------------------------------------------------");
        LOG.info("");

        LOG.info("..... Tile Library ......");
        tileLibrary.loadResources(data -> new TileTemplateImpl(this, data));
        LOG.info("");

        LOG.info("..... Plan Library ......");
        planLibrary.loadResources(data -> new PlanImpl(this, data));
        LOG.info("");

        LOG.info("..... Audio Library .....");
        audioCommunicationService.loadResources();
        LOG.info("");

        LOG.info("Game Library was successfully initialized...");
        state = State.READY;

    }

    /**
     * Returns the game resource with the passed ID. This method checks all available {@link ResourceLibrary}s for the given resource and tries to cast the result to the requested
     * type.
     *
     * @param id  the id
     * @param <T> the type of the requested game resource
     *
     * @return the resource
     *
     * @throws ClassCastException       if the type of the resource does not match the expected one
     * @throws IllegalArgumentException if the id does not exist.
     */
    public <T extends GameResource> T getById(String id) {

        checkInitialization();

        for (ResourceLibrary lib : libraries) {
            Object o = lib.getById(id);
            if (o != null) {
                return (T) o;
            }
        }
        throw new IllegalArgumentException("A game resource with ID ::= [" + id + "] does not exist");
    }

    /**
     * Method to check if the resources were loaded by calling the {@link #loadResources()} method
     *
     * @throws IllegalStateException if the method was not called
     */
    private void checkInitialization() {
        if (state == State.CREATED) {
            throw new IllegalStateException("GameLibrary was not initialized by calling the loadResources() method.");
        }
    }

}

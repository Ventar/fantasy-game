package mro.fantasy.game.engine;

import mro.fantasy.game.engine.plan.Plan;
import mro.fantasy.game.engine.plan.TileTemplate;
import mro.fantasy.game.resources.GameResource;
import mro.fantasy.game.resources.ResourceLibrary;
import mro.fantasy.game.engine.plan.PlanLibrary;
import mro.fantasy.game.engine.plan.TileLibrary;
import mro.fantasy.game.engine.plan.impl.PlanImpl;
import mro.fantasy.game.engine.plan.impl.TileTemplateImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.function.Function;

/**
 * The overall library that provides {@link GameResource}s.
 */
@Service
public class GameLibrary {

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
        return tileLibrary;
    }

    /**
     * Returns the library with all {@link Plan}s known by the current game installation
     *
     * @return the library
     */
    public PlanLibrary getPlanLibrary() {
        return planLibrary;
    }

    /**
     * Calls the {@link ResourceLibrary#loadResources(Function)} method for every library to load all needed data. Some of the libraries
     * depend on each other so that this method is responsible for the order.
     */
    @PostConstruct
    public void loadResources() {
        tileLibrary.loadResources(data -> new TileTemplateImpl(this, data));
        planLibrary.loadResources(data -> new PlanImpl(this, data));
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
        for (ResourceLibrary lib : libraries) {
            Object o = lib.getById(id);
            if (o != null) {
                return (T) o;
            }
        }
        throw new IllegalArgumentException("A game resource with ID ::= [" + id + "] does not exist");
    }
}

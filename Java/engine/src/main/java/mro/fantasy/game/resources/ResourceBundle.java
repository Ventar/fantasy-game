package mro.fantasy.game.resources;

import mro.fantasy.game.engine.GameLibrary;
import mro.fantasy.game.engine.plan.Plan;
import mro.fantasy.game.engine.plan.Tile;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Bundle with resources which can be created by a {@link ResourceBundleProvider}.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-04
 */
public interface ResourceBundle<T extends GameResource> {

    /**
     * Returns the resource with the given unique ID. The resources have to be immutable within the bundle
     *
     * @param id the unique ID of the resource
     *
     * @return the resource or {@code null} if the resource does not exist.
     */
    T getById(String id);

    /**
     * Returns a list of all resources which are managed by this bundle. In larger applications a method to fetch all and everything wouldn't be a good idea but for the game engine
     * only a limited number of resources of every type is expected.
     *
     * @return a list of all resources
     */
    List<T> getAll();

    /**
     * Loads the resources of the resource bundle. The method is called by the {@link ResourceLibrary} (which is triggered by the {@link GameLibrary} {@link
     * javax.annotation.PostConstruct} method) and loads all data from the underlying {@link ResourceBundle}.
     * <p>
     * Resources may depend on each other, i.e. the passed game library may not be fully filled with all data. While resources like {@link Tile}s
     * are completely independent, a {@link Plan} depends on the tiles from the {@link mro.fantasy.game.engine.plan.TileLibrary}. The correct
     * order of the call to the {@link #loadResources(Function)} method is managed in the {@link GameLibrary}.
     *
     * @param builder the builder function to convert generic YAML data into a GameResource of type T
     */
    void loadResources(Function<Map<String, Object>, T> builder);


}

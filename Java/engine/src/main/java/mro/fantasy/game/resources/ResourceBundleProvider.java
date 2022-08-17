package mro.fantasy.game.resources;

import mro.fantasy.game.engine.character.equpipment.impl.EquipmentImpl;
import mro.fantasy.game.plan.Plan;
import mro.fantasy.game.resources.impl.ClasspathResourceBundleProvider;

import java.util.List;

/**
 * The various game elements like {@link mro.fantasy.game.engine.character.Character}s, {@link EquipmentImpl}, {@link
 * Plan}s, etc. are backed by YAML files and can be loaded with the help of providers.
 * <p>
 * Various JAR files can provide providers to load data from either the classpath, a database or the file system. The default implementation in this case is the {@link
 * ClasspathResourceBundleProvider} which scans a configured directory for a certain type of resource.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-05
 */
public interface ResourceBundleProvider<R extends GameResource, T extends ResourceBundle<R>> {

    /**
     * Returns a list of bundles.
     *
     * @return a list of bundles
     */
    List<T> getResourceBundles();


}

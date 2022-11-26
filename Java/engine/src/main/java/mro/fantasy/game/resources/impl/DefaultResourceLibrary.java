package mro.fantasy.game.resources.impl;

import mro.fantasy.game.resources.GameResource;
import mro.fantasy.game.resources.ResourceBundle;
import mro.fantasy.game.resources.ResourceBundleProvider;
import mro.fantasy.game.resources.ResourceLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default implementation of a resource library for a given type of {@link GameResource}.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-05
 */
public class DefaultResourceLibrary<T extends GameResource> implements ResourceLibrary<T> {

    /**
     * Logger
     */
    public static final Logger LOG = LoggerFactory.getLogger(DefaultResourceLibrary.class);

    /**
     * A list of providers to fetch the resources.
     */
    @Autowired
    private List<ResourceBundleProvider<T, ResourceBundle<T>>> resourceProvider;

    /**
     * A map with all resources in all bundles. Key is the unique {@link GameResource#getGameId()} of the resource.
     */
    private Map<String, T> resources;

    @Override
    public void loadResources(Function<Map<String, Object>, T> builder) {

        if (resourceProvider == null) {     // Should not happen because of the default classpath provider
            return;
        }

        this.resourceProvider.forEach(p -> p.loadResources());

        this.resources = resourceProvider.stream()                                      // iterate over all resource provider
                .map(ResourceBundleProvider::getResourceBundles)                        // fetch all available resources bundles from that provider
                .flatMap(Collection::stream)
                .flatMap(bundle -> {                                                    // load the resources
                    bundle.loadResources(builder);
                    return bundle.getAll().stream();
                })
                .collect(Collectors.toMap(                                              // add a new map entry with the id to
                        GameResource::getGameId, res -> res));                          // the resource map to allow efficient access to single resources

        LOG.debug("Created new library with ::= [{}] resources from ::= [{}] resource providers", resources.size(), resourceProvider.size());
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public T getById(String id) {

        if (resources == null) {
            return null;
        }

        return resources.get(id);
    }

    @Override
    public List<T> getAll() {

        if (resources == null) {
            return Collections.emptyList();
        }

        return List.copyOf(resources.values());
    }

}

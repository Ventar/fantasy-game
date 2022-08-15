package mro.fantasy.game.resources.impl;

import mro.fantasy.game.resources.GameResource;
import mro.fantasy.game.resources.ResourceBundle;
import mro.fantasy.game.utils.YAMLUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default implementation of a resource bundle that takes a {@link Resource} and tries to parse it.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-04
 */
public final class DefaultResourceBundle<T extends GameResource> implements ResourceBundle<T> {

    /**
     * Logger
     */
    public static final Logger LOG = LoggerFactory.getLogger(DefaultResourceBundle.class);

    /**
     * The unique name of the bundle
     */
    private String name;

    /**
     * A map with all RESOURCES in this bundle.
     */
    private Map<String, T> resources;

    /**
     * The underlying resource stream that contains the data for the bundle.
     */
    private final Resource inputResource;

    /**
     * Initializes the bundle from the passed  {@link Resource}. A resource bundle has always the structure:
     * <p>
     * <pre>{@code
     * bundleName: Base Game
     * resources:
     *   - ...
     * }</pre>
     * i.e. it has a name and a list of resources.
     *
     * @param res the resource in YAML format
     *
     * @throws IllegalArgumentException in case the data cannot be parsed
     */
    public DefaultResourceBundle(Resource res) {
        this.inputResource = res;
    }

    @Override
    public void loadResources(Function<Map<String, Object>, T> builder) {
        try {
            var yaml = new Yaml();
            Map<String, Object> map = yaml.load(inputResource.getInputStream());
            var yamlResources = (List<Map<String, Object>>) map.get("resources");

            LOG.debug("Loaded  YAML with ::= [{}] resource entries", yamlResources.size());

            this.resources = yamlResources
                    .stream()                                             // iterate over all Resource entries in the list
                    .map(builder)                                         // convert the YAML map to a Java class and convert it
                    .collect(Collectors.toMap(r -> r.getGameId(), r -> r));   // and create the resource map

            this.name = YAMLUtilities.getMandatory(map, "bundleName");

            LOG.debug("Created resource bundle ::= [{}]", this.name);

        } catch (IOException e) {
            LOG.warn("Cannot load resource bundle from resource ::= [{}]: ", resources, e);
        }
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

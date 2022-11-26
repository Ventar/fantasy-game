package mro.fantasy.game.resources.impl;

import mro.fantasy.game.resources.GameResource;
import mro.fantasy.game.resources.ResourceBundle;
import mro.fantasy.game.resources.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Utility class that scans the Java classpath for YAML files and convert them to Java classes.
 *
 * @param <T> the type of the content
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-04
 */
public class ClasspathResourceBundleProvider<R extends GameResource, T extends ResourceBundle<R>> implements ResourceBundleProvider<R, T> {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ClasspathResourceBundleProvider.class);

    /**
     * The list of resource file content.
     */
    private List<T> resourceFileContent;

    /**
     * The function that is used to build the content instance.
     */
    private Function<Resource, T> builder;

    /**
     * The directory in the classpath that is scanned for YAML files.
     */
    private String directory;

    /**
     * Indicator if the {@link #loadResources()} method of this class was executed once. If not an exception is thrown.
     */
    private boolean initialized;

    /**
     * Creates a new provider that scans the passed directory for yaml files and tries to resolve them.
     *
     * @param directory the directory to scan
     * @param builder   the function that is used to build the data class <T> instance.
     */
    public ClasspathResourceBundleProvider(String directory, Function<Resource, T> builder) {
        this.directory = directory;
        this.builder = builder;
    }

    /**
     * Creates a new {@link ClasspathResourceBundleProvider} that creates {@link DefaultResourceBundle} from the passed directory.
     *
     * @param directory the directory to scan
     *
     * @return the provider
     */
    public static ClasspathResourceBundleProvider forDefaultResourceBundle(String directory) {
        return new ClasspathResourceBundleProvider(directory, res -> new DefaultResourceBundle((Resource) res));
    }

    @Override
    public void loadResources() {

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources;

        try {
            resources = resolver.getResources("classpath*:/" + directory + "/*.yaml");
        } catch (IOException e) {
            LOG.error("Could not fetch tile bundles: ", e);
            return;
        }

        LOG.info("ClasspathResourceBundleProvider: Found ::= [{}] potential YAML files in path ::= [{}]", resources.length, directory);

        this.resourceFileContent = Arrays                                                                 // try to create tile bundles for every YAML file
                .stream(resources)
                .map(res -> {
                    try {
                        LOG.info("Try to create resource bundle from :.= [{}]", res.getURL().getFile());
                        return builder.apply(res);
                    } catch (Exception e) {
                        if (LOG.isTraceEnabled()) {                                                       // on TRACE we will print the complete stacktrace but for all other
                            LOG.warn("Could not load resource bundle: ", e);                              // log levels, only the message is shown in a warning.
                        } else {
                            LOG.warn("Could not create resource bundle (enable TRACE for more information): {}", e.getMessage());
                        }
                        return null;
                    }
                })
                .filter(Objects::nonNull)                                                                // in case of an exception the YAML file is ignored
                .toList();

        LOG.info("ClasspathResourceBundleProvider: Initialization of resource provider for directory ::= [{}] DONE", directory);
        initialized = true;

    }

    @Override
    public List<T> getResourceBundles() {
        if (!initialized) {
            throw new IllegalStateException("ClasspathResourceBundleProvider for directory ::= [" + directory + "] was not initialized by calling rhe loadResources() method.");
        }

        return Collections.unmodifiableList(resourceFileContent);
    }

}

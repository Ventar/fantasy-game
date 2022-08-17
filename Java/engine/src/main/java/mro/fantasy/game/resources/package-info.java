/**
 * Resource management for static game data.
 * <h2>Resources, Resource Bundles and Providers</h2>
 * Within the game there are multiple resources which are used to build up the game system. Examples for resources are
 * <ul>
 *   <li>{@link mro.fantasy.game.plan.Plan}s,</li>
 *   <li>their {@link   mro.fantasy.game.plan.TileTemplate}s,</li>
 *   <li>{@link mro.fantasy.game.engine.character.Character}s or</li>
 *   <li>{@link mro.fantasy.game.communication.impl.AudioResource}s</li>
 * </ul>
 * <p>
 * What resources have in common is that they have multiple concrete implementations and can be extended easily with the help of files, APIs or databases. The idea behind resources
 * is to allow an easy extension of existing data. All resources are stored in {@link mro.fantasy.game.resources.ResourceBundle}s and can be loaded during the application startup
 * with a concrete implementation of a {@link mro.fantasy.game.resources.ResourceBundleProvider}.
 * <p>
 * All resources of a type are made available to the game engine via a {@link mro.fantasy.game.resources.ResourceLibrary} which implements the same methods as a simple resource
 * bundle but scans the classpath for all resource bundle providers during startup and makes all resources available even if they are provided by multiple providers.
 * <p>
 * <img src="doc-files/ResourceBundles.png"/>
 * <h2>Loading Resources</h2>
 * Resources are loaded during the startup of the application. The default implementation of a resource provider is the
 * {@link mro.fantasy.game.resources.impl.ClasspathResourceBundleProvider} that scans the classpath for YAML files that contain the resources. To differentiate various types of
 * resources the generic classpath provider has to be instantiated with a directory to scan, i.e. it is not created automatically via a spring annotation but has to be
 * constructed manually during startup. This is achieved with the help of the Spring @{@link org.springframework.boot.autoconfigure.AutoConfiguration} annotation:
 * <pre>{@code
 *
 *  @AutoConfiguration
 *  public class ApplikationConfiguration {
 *
 *     @Bean
 *     public ClasspathResourceBundleProvider<TileTemplate, ResourceBundle<TileTemplate>> getTileProvider() {
 *         return ClasspathResourceBundleProvider.forDefaultResourceBundle("tiles");
 *     }
 *
 *     ...
 *  }
 * }</pre>
 * The default implementation will scan the root of all JAR files in the classpath and check the 'tiles' folder for files with the YAML extension. If these files are found a
 * resource bundle is created from the YAML file.
 *
 * <h2>Concrete Libraries and the GameLibrary</h2>
 * Since resources can depend on each other the process of loading them has to have a certain order. Within a YAML file resources can refer to other resources by the unique
 * {@link mro.fantasy.game.resources.GameResource#getGameId()}. To achieve this order, loading the resources is not performed immediately when a resource bundle is created but
 * later in the {@link mro.fantasy.game.engine.GameLibrary}. The game library is a wrapper for all resource libraries which are used by the game engine and offers access to
 * all available resources. It aggregates concrete instances of resource libraries and calls the
 * {@link mro.fantasy.game.resources.ResourceLibrary#loadResources(java.util.function.Function)} in the correct order during the Spring {@code @PostConstruct }  phase. All
 * {@link mro.fantasy.game.plan.Plan}s for example are loaded after the {@link mro.fantasy.game.plan.TileTemplate}s which it uses.
 * <p>
 * <img src="doc-files/GameLibrary.png"/>
 */
package mro.fantasy.game.resources;
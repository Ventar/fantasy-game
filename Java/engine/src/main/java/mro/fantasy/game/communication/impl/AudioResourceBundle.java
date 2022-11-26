package mro.fantasy.game.communication.impl;

import mro.fantasy.game.communication.AudioCommunicationService;
import mro.fantasy.game.resources.ResourceBundle;
import mro.fantasy.game.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Resource bundle with a list of communication resources. Each bundle is based on an YAML streams that follows the format described below. These files can reside in various
 * location, for example the classpath of a JAR file or the OS file system. During the instantiation of the implementing classes the input stream is processed and all translation
 * resources are loaded and converted into @{@link AudioResource} objects that can be used by the application.
 * <pre>{@code
 *   bundleName: whatever
 *   locale: en
 *   voiceId: Matthew
 *   resources:
 *   - key: player.1x1
 *     hash: 7750d93d025e9b68ef6010ec09542740ff0fa1acf1e21a04c4d2fbe772bb625e
 *     text: |
 *       <speak>
 *         <say-as interpret-as="digits">001</say-as>
 *       </speak>
 *   - key: cultist.1x1
 *     hash: 1e6635d15df1000e88631218a22b025667796e8f6291662660711867b4276548
 *     text: |
 *       <speak>
 *         <say-as interpret-as="digits">002</say-as>
 *       </speak>
 * }</pre>
 * <p>
 * <b>field descriptions</b>
 * <table summary="field descriptions">>
 *     <tr>
 *         <th><b>field</b></th>
 *         <th><b>description</b></th>
 *     </tr>
 *     <tr>
 *         <td>bundleName</td>
 *         <td>the bundle name can be any value and is used in the {@link AudioCommunicationService} to access resources from this bundle</td>
 *     </tr>
 *     <tr>
 *         <td>locale</td>
 *         <td>the locale has to match a valid Java {@link Locale} language identifier and is used together with the bundleName and the key to find a specific resource</td>
 *     </tr>
 *     <tr>
 *         <td>resources</td>
 *         <td>a list of resources managed in this file</td>
 *     </tr>
 *     <tr>
 *         <td>translation</td>
 *         <td>part of the resources list; represents a single translation</td>
 *     </tr>
 *     <tr>
 *         <td>key</td>
 *         <td>the key of the translation resource. Together with the locale it represents the unique identifier for the text</td>
 *     </tr>
 *     <tr>
 *         <td>hash</td>
 *         <td>The hash field is an optional one that can be empty. In combination with the
 *         {@link AWSSpeechSynthesizer} service
 *         this is an indicator that the resource was added and has to be synthesized. This value is managed by the application and is build from the normalized
 *         version in the text field. In case this field has a different hash than the one in this field, this is an indicator for the synthesizer,too</td>
 *     </tr>
 *     <tr>
 *         <td>ssml</td>
 *         <td>the actual text of the resource in SSML format that can be either displayed or be synthesized</td>
 *     </tr>
 * </table>
 *
 * @author Michael Rodenbuecher
 * @since 2022-07-18
 */
public class AudioResourceBundle implements ResourceBundle<AudioResource> {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AudioResourceBundle.class);

    /**
     * String constant.
     */
    private static final String STR_RESOURCES = "resources";

    /**
     * The name of the bundle.
     */
    private String bundleName;

    /**
     * The locale of the bundle.
     */
    private Locale locale;

    /**
     * Function to load an MP3 file for a resource.
     */
    private final AudioResourceMP3FileResolver mp3StreamFunction;

    /**
     * Contains the keys of the audio resources in the order they were loaded from the YAML file. This is used to preserve the order when writing back the resources to a new file.
     * While this is not necessary from a technical point of view it makes life easier for developers which have to modify the resource files.
     */
    private List<String> orderedResourceKeys;

    /**
     * The map with the communication resources.
     */
    private Map<String, AudioResource> resources;

    /**
     * Creates a new resource bundle from the passed input stream. The input stream has to be an YAML file that follows the format described in {@link AudioResourceBundle}. During
     * the instantiation of this class the input stream is processed and all translation resources are loaded and converted into @{@link AudioResource} objects that can be used by
     * the application.
     *
     * @param res      the stream with the YAML data to construct the {@link AudioResource}s
     * @param resolver the resolver function to load the MP3 input stream that belongs to the resource. The MP3 files can be stored in the file system, as a classpath resource or
     *                 be rendered on the fly depending on the underlying technical solution.
     *
     * @throws IllegalArgumentException in case the passed input stream cannot be parsed.
     */
    public AudioResourceBundle(Resource res, AudioResourceMP3FileResolver resolver) {
        Objects.requireNonNull(res, "The resource dataInputStream cannot be null.");

        // if no stream function is provided, which can be the case if this bundle is used by the speech synthesizer we add a default implementation
        // to throw an exception, just to be on the safe side.

        if (resolver == null) {
            LOG.warn("MP3 stream function is null, set default implementation which will throw an exception. This should only be used in conjunction with a SpeechSynthesizer");
            this.mp3StreamFunction = new AudioResourceMP3FileResolver() {
                @Override
                public InputStream resolve(AudioResource audioResource) {
                    throw new IllegalArgumentException("No implementation available to resolve MP3 streams.");
                }

                @Override
                public String getStreamInfo(AudioResource audioResource) {
                    throw new IllegalArgumentException("No implementation available to resolve MP3 streams.");
                }
            };
        } else {
            this.mp3StreamFunction = resolver;
        }

        try {
            loadData(res.getInputStream(), null);
        } catch (IOException e) {
            LOG.warn("Cannot load resource bundle from resource ::= [{}]: ", resources, e);
        }

    }

    /**
     * Creates a new resource bundle from the passed input stream. The input stream has to be an YAML file that follows the format described in {@link AudioResourceBundle}. During
     * the instantiation of this class the input stream is processed and all translation resources are loaded and converted into @{@link AudioResource} objects that can be used by
     * the application.
     *
     * @param dataInputStream the stream with the YAML data to construct the {@link AudioResource}s
     * @param resolver        the resolver function to load the MP3 input stream that belongs to the resource. The MP3 files can be stored in the file system, as a classpath
     *                        resource or be rendered on the fly depending on the underlying technical solution.
     *
     * @throws IllegalArgumentException in case the passed input stream cannot be parsed.
     */
    public AudioResourceBundle(InputStream dataInputStream, AudioResourceMP3FileResolver resolver) {

        Objects.requireNonNull(dataInputStream, "The resource dataInputStream cannot be null.");

        // if no stream function is provided, which can be the case if this bundle is used by the speech synthesizer we add a default implementation
        // to throw an exception, just to be on the safe side.

        if (resolver == null) {
            LOG.warn("MP3 stream function is null, set default implementation which will throw an exception. This should only be used in conjunction with a SpeechSynthesizer");
            this.mp3StreamFunction = new AudioResourceMP3FileResolver() {
                @Override
                public InputStream resolve(AudioResource audioResource) {
                    throw new IllegalArgumentException("No implementation available to resolve MP3 streams.");
                }

                @Override
                public String getStreamInfo(AudioResource audioResource) {
                    throw new IllegalArgumentException("No implementation available to resolve MP3 streams.");
                }
            };
        } else {
            this.mp3StreamFunction = resolver;
        }

        loadData(dataInputStream, null);

    }

    @Override
    public void loadResources(Function<Map<String, Object>, AudioResource> builder) {
        // not needed here
    }

    /**
     * Loads the audio resource information from the YAML stream.
     *
     * @param dataInputStream    the stream with the YAML data to construct the {@link AudioResource}s
     * @param bundleNameCallback callback triggered when the bundle name was resolved. Necessary workaround to generate the default {@link
     *                           AudioResourceMP3FileResolver.ClasspathMP3FileResolver} which depends on the bundle name to resolve MP3 files.
     */
    private void loadData(InputStream dataInputStream, Consumer<String> bundleNameCallback) {
        try {
            Yaml yaml = new Yaml(); // not thread-safe !
            Map<String, Object> yamlData = yaml.load(dataInputStream);

            ValidationUtils.requireNonNull(yamlData.get(STR_RESOURCES), "There are no resources available");


            this.bundleName = Optional.ofNullable(yamlData.get("bundleName")).orElseThrow(() -> new IllegalArgumentException("The bundleName field is missing.")).toString();
            this.locale = new Locale(Optional.ofNullable(yamlData.get("locale")).orElseThrow(() -> new IllegalArgumentException("The locale field is invalid.")).toString());

            Optional.ofNullable(bundleNameCallback).ifPresent(c -> c.accept(bundleName)); // if a callback was passed, trigger it here

            var orderedResourceList =
                    ((List<Map<String, Object>>) yamlData.get(STR_RESOURCES))                          // load the data from the yaml file
                            .stream()                                                                // process them as stream and
                            .map(r -> AudioResource.fromYAMLMap(this, r, mp3StreamFunction))   // and create a HashMap with the resource key as key and the resource as value
                            .toList();

            this.orderedResourceKeys = orderedResourceList
                    .stream()
                    .map(AudioResource::getKey)                     // simply take the key to add it to the list
                    .toList();

            this.resources = orderedResourceList
                    .stream()
                    .collect(Collectors.toMap(AudioResource::getKey, r -> r));   // create a HashMap with the resource key as key and the resource as value

            LOG.info("Loaded communication resource bundle ::= [{}] with locale ::= [{}] and ::= [{}] entries", bundleName, locale, resources.size());

            dataInputStream.close();


            this.resources.values().forEach(r -> LOG.info("    - {} [{}]", String.format("%-30s", r.getGameId()), mp3StreamFunction.getStreamInfo(r)));

        } catch (IllegalArgumentException e) {
            throw e; // simply rethrow.
        } catch (Exception e) {
            // Convert to a runtime exception related to the player communication service
            throw new IllegalArgumentException("Cannot parse the passed input stream:", e);
        }
    }

    /**
     * Normalizes the bundle name, e.g. to be used as directory in the file system. Replaces all blanks with underscores, converts all characters to lower case and remove all
     * characters beside letters and numbers.
     *
     * @return the normalized bundle name
     */
    public static String normalizeBundleName(String filename) {
        return filename.substring(0, filename.indexOf(".yaml"))
                .replace(" ", "_")
                .replaceAll("\\W", "")
                .toLowerCase(Locale.ROOT);
    }

    /**
     * Writes the complete resource bundles in the YAML format to the passed writer.
     *
     * @param writer the writer to write to
     */
    public void writeYAML(Writer writer) {
        // use a linked hashmap here to preserve the order of the attributes as defined in the example
        var yamlRoot = new LinkedHashMap<String, Object>();              // the root element of the YAML file with the bundleName, the local and the resources list
        var yamlResources = new ArrayList<Map<String, Object>>();        // the list with the audio resource description

        yamlRoot.put("bundleName", bundleName);
        yamlRoot.put("locale", locale.getLanguage());
        yamlRoot.put(STR_RESOURCES, yamlResources);

        this.orderedResourceKeys                                          // contains all keys of the underlying input stream in the correct order.
                .stream()
                .map(this::getById)                                       // fetch the resource based on the key
                .forEachOrdered(r -> yamlResources.add(r.toYAMLMap()));   // and convert it to a YAML map that is added to the resources list in the root container

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        Yaml yaml = new Yaml(options); // not thread-safe !
        yaml.dump(yamlRoot, writer);

    }

    /**
     * The name of the resource bundle.
     *
     * @return the name
     */
    public String getName() {
        return bundleName;
    }

    /**
     * The locale of the resource bundle
     *
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    @Override
    public AudioResource getById(String id) {
        if (resources == null) {
            return null;
        }
        return resources.get(id);
    }

    @Override
    public List<AudioResource> getAll() {
        return List.copyOf(resources.values());
    }

    @Override
    public String toString() {
        return "AudioResourceBundle{" +
                       "bundleName='" + bundleName + '\'' +
                       ", locale=" + locale +
                       '}';
    }

}

package mro.fantasy.game.engine.communication.impl;


import mro.fantasy.game.engine.communication.AudioCommunicationService;
import mro.fantasy.game.engine.communication.data.AudioResource;
import mro.fantasy.game.engine.communication.data.AudioResourceBundle;
import mro.fantasy.game.engine.communication.data.AudioResourcePlayTask;
import mro.fantasy.game.resources.ResourceBundle;
import mro.fantasy.game.resources.ResourceBundleProvider;
import mro.fantasy.game.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * Spring service to play audio files back to the player. The service handles multiple {@link AudioResourceBundle}s and their internal {@link AudioResource}s.
 *
 * @author Michael Rodenbuecher
 * @since 2022-07-23
 */
@Service
public class AudioCommunicationServiceImpl implements AudioCommunicationService {

    /**
     * Logger
     */
    public static final Logger LOG = LoggerFactory.getLogger(AudioCommunicationServiceImpl.class);

    /**
     * Combination of bundle and key to identify an audio resource.
     *
     * @author Michael Rodenbuecher
     * @since 2022-07-24
     */
    private record ResourceKey(String bundle, String key, Locale locale) {}

    /**
     * Executor to play audio files.
     */
    private final ThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    /**
     * List of futures for audio files which are played.
     */
    private final List<AudioResourcePlayTask> playQueue = new ArrayList<>();

    /**
     * A list of resource providers to provide access to the underlying MP3 streams.
     */
    @Autowired
    private List<ResourceBundleProvider<AudioResource, AudioResourceBundle>> resourceProvider;

    /**
     * Map with all available audio resources from the {@link #resourceProvider} list. During the initialization this map is filled with data to access audio resources efficient.
     */
    private Map<ResourceKey, AudioResource> resourceMap;

    /**
     * Initialize the service by collection all {@link AudioResource}s which are made available through the {@link ResourceBundleProvider}s and their {@link AudioResourceBundle}s
     * in a single map for quick access.
     *
     * @throws IllegalArgumentException in case multiple resources with the same unique combination of key, bundle and locale are provided by the resource providers.
     */
    @PostConstruct
    private void postConstruct() {

        if (resourceProvider == null) {     // Should not happen because of the default classpath provider
            return;
        }

        resourceMap = resourceProvider.stream()                                         // iterate over all resource provider
                .map(ResourceBundleProvider::getResourceBundles)                        // fetch all available audio resource bundles from that provider
                .flatMap(Collection::stream)                                            // ...convert to a new stream...
                .map(ResourceBundle::getAll)                                            // fetch all audio resources from the bundle
                .flatMap(Collection::stream)                                            // ...convert to a new stream...
                .collect(Collectors.toMap(                                              // add a new map entry with a ResourceKey type and the audio resource to
                        res -> new ResourceKey(                                         // the resource map to allow efficient access to single audio resources
                                res.getBundle().getName(),
                                res.getKey(),
                                res.getBundle().getLocale()
                        ), res -> res));

        LOG.debug("Created new AudioCommunicationService with ::= [{}] audio resources", resourceMap.size());

    }

    @Override
    public void play(String bundleName, String key, Locale locale, AudioCommunicationService.AudioVariable... variables) {
        play(bundleName, key, locale, false, variables);
    }

    @Override
    public void playSync(String bundleName, String key, Locale locale, AudioCommunicationService.AudioVariable... variables) {
        play(bundleName, key, locale, true, variables);
    }

    /**
     * Starts the playback of the audio resource which matches the given combination of attributes.
     *
     * @param bundleName the bundle name of the audio resource to play
     * @param key        the key of the resource to play.
     * @param locale     the locale of the resource to play
     * @param sync       if the execution of the current thread should be blocked until the audio resources was played.
     * @param variables  (optional) variables for an audio resource with placeholders.
     *
     * @throws IllegalArgumentException if the resource cannot be found or validation fails
     */
    private void play(String bundleName, String key, Locale locale, boolean sync, AudioVariable... variables) {

        LOG.debug("Try to play audio resource with bundle :Ö:= [{}], key ::= [{}], locale ::= [{}], sync ::= [{}], variables ::= [{}]", bundleName, key, locale.getLanguage(),
                sync, variables);

        ValidationUtils.requireNonNull(bundleName, "The parameter bundeName cannot be null");
        ValidationUtils.requireNonNull(key, "The key bundeName cannot be null");
        ValidationUtils.requireNonNull(locale, "The locale bundeName cannot be null");

        ResourceKey resourceKey = new ResourceKey(bundleName, key, locale);
        ValidationUtils.requireTrue(resourceMap.containsKey(resourceKey),          // validate the existence of the basic resource
                "Cannot find an audio resource that matches bundle name ::= [" + bundleName + "], key ::= [" + key + "], locale ::= [" + locale.getLanguage() + "]");

        Arrays.stream(variables).forEach(variable ->                                          // validate every variable if present
                                                 ValidationUtils.requireTrue(
                                                         resourceMap.containsKey(new ResourceKey(variable.getAudioBundle(), variable.getAudioKey(), locale)),
                                                         "Cannot find an audio resource that matches bundle name ::= [" + variable.getAudioBundle() + "], key ::= [" + variable.getAudioKey() + "], " +
                                                                 "locale ::= [" + locale.getLanguage() + "]"));

        playAudio(
                resourceMap.get(resourceKey),
                sync,
                Arrays.stream(variables).map(
                        var -> resourceMap.get(new ResourceKey(var.getAudioBundle(), var.getAudioKey(), locale))).toArray(AudioResource[]::new));

    }


    /**
     * Play the passed audio resource.
     *
     * @param resource the resources to play.
     * @param sync     if the execution of the current thread should be blocked until the audio resources were played.
     */
    private void playAudio(AudioResource resource, boolean sync, AudioResource... variables) {

        playQueue.removeIf(task -> task.getState() == AudioResourcePlayTask.State.STOPPED);                    // remove all played resources from the queue

        var task = new AudioResourcePlayTask(resource, variables);
        playQueue.add(task);

        var f = executor.submit(task);

        LOG.debug("Added resource ::= [{}] to the play queue", resource);

        if (sync) {         // block until the audio task finished playing.
            try {
                f.get();
            } catch (Exception e) {
                LOG.warn("Could not play task", task);
            }
        }

    }


}

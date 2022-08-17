package mro.fantasy.game.communication;

import java.util.Locale;

/**
 * Service to play back audio messages to the player.
 *
 * @author Michael Rodenbuecher
 * @since 2022-07-24
 */
public interface AudioCommunicationService {

    /**
     * Utility class to allow the caller of the {@link #play(String, String, Locale, AudioVariable...)} method to pass arguments for audio resources with placeholders.
     * <p>
     * The variable does not have a {@link Locale} since it makes no sense to mix different locales in a single audio stream. During the construction of the final stream the locale
     * of the original audio resource is used.
     *
     * @author Michael Rodenbuecher
     * @since 2022-07-24
     */
    interface AudioVariable {
        /**
         * Returns the name of the audio bundle that is used to find the underlying MP3 stream for the variable.
         *
         * @return the bundle name
         */
        String getAudioBundle();

        /**
         * Returns the name of the audio key that is used to find the underlying MP3 stream for the variable.
         *
         * @return the key
         */
        String getAudioKey();
    }

    /**
     * Starts the playback of the audio resource which matches the given combination of attributes.
     * <p>
     * The execution is not blocked, i.e. the code execution is immediately continued.
     *
     * @param bundleName the bundle name of the audio resource to play
     * @param key        the key of the resource to play.
     * @param locale     the locale of the resource to play
     * @param variables  (optional) variables for an audio resource with placeholders.
     *
     * @throws IllegalArgumentException if the resource cannot be found or variables do not match the resource.
     */
    void play(String bundleName, String key, Locale locale, AudioVariable... variables);

    /**
     * Starts the playback of the audio resource which matches the given combination of attributes.
     * <p>
     * The execution is blocked, i.e. the code execution will continue when the audio file was played completely.
     *
     * @param bundleName the bundle name of the audio resource to play
     * @param key        the key of the resource to play.
     * @param locale     the locale of the resource to play
     * @param variables  (optional) variables for an audio resource with placeholders.
     *
     * @throws IllegalArgumentException if the resource cannot be found variables do not match the resource.
     */
    void playSync(String bundleName, String key, Locale locale, AudioVariable... variables);
}

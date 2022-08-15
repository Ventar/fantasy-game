package mro.fantasy.game.engine.communication.impl;

import mro.fantasy.game.engine.communication.data.AudioResource;
import mro.fantasy.game.engine.communication.data.AudioResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Optional;

/**
 * Interface to resolve a resource key to an MP3 input stream. The implementation is called in the {@link AudioResource#getMP3Stream(AudioResource...)} method to build the input
 * streams to perform a playback to the players. This interface is needed to support different storage formats for the MP3 files, i.e. directly in the file system, created on the
 * fly from a web service or from the classpath.
 *
 * @author Michael Rodenbuecher
 * @since 2022-07-19
 */
public interface AudioResourceMP3FileResolver {

    /**
     * Default implementation of a resolver to load MP3 files that were stored in the resource directory of a Maven project with help of the {@link AudioResourceFileManager} and
     * should be loaded by the {@link Class#getResourceAsStream(String)} method.
     *
     * @author Michael Rodenbuecher
     * @since 2022-07-20
     */
    final class ClasspathMP3FileResolver implements AudioResourceMP3FileResolver {

        /**
         * Logger
         */
        public static final Logger LOG = LoggerFactory.getLogger(AudioResourceBundle.class);

        /**
         * The directory in the JAR file in which the mp3 files for the {@link AudioResourceBundle} are stored
         */
        private String mp3Directory;

        /**
         * @param mp3Directory the directory in the classpath where the MP3 files are stored. Has to be an absolute path, if not, it will be made absolute by prepending a /
         */
        public ClasspathMP3FileResolver(String mp3Directory) {
            this.mp3Directory = "";

            if (!mp3Directory.startsWith("/")) {
                this.mp3Directory += "/";           // as mentioned in the JavaDoc ensure that the directory is absolute.
            }

            this.mp3Directory += mp3Directory;

            if (!mp3Directory.endsWith("/")) {
                this.mp3Directory += "/";           // normalize the directory for the usage in the resolve method
            }

            LOG.debug("Created ClasspathMP3Resolver for directory ::= [{}]", mp3Directory);

        }

        @Override
        public InputStream resolve(AudioResource audioResource) {
            LOG.debug("Try to resolve MP3 file ::= [{}]", this.mp3Directory + audioResource.getMP3FileName());

            return Optional
                    .ofNullable(getClass().getResourceAsStream(this.mp3Directory + audioResource.getMP3FileName()))
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find MP3 file for resource ::= [" + audioResource + "]"));
        }
    }

    /**
     * Resolves the passed audio resource to the underlying MP3 stream.
     *
     * @param audioResource the resource
     *
     * @return the MP3 stream
     *
     * @throws IllegalArgumentException if the resource cannot be resolved
     */
    InputStream resolve(AudioResource audioResource);

}

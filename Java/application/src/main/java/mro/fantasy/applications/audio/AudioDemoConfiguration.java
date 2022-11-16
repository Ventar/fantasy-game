package mro.fantasy.applications.audio;

import mro.fantasy.game.communication.impl.AudioResource;
import mro.fantasy.game.communication.impl.AudioResourceBundle;
import mro.fantasy.game.communication.impl.AudioResourceMP3FileResolver;
import mro.fantasy.game.resources.impl.ClasspathResourceBundleProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class AudioDemoConfiguration {

    @Bean
    public ClasspathResourceBundleProvider<AudioResource, AudioResourceBundle> getAudioResourceProvider() {
        return new ClasspathResourceBundleProvider<AudioResource, AudioResourceBundle>("mp3",
                (res) -> new AudioResourceBundle(res,
                        new AudioResourceMP3FileResolver.ClasspathMP3FileResolver("/mp3/data/" + AudioResourceBundle.normalizeBundleName(res.getFilename()))));
    }


}

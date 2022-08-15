package mro.fantasy.game;

import mro.fantasy.game.engine.communication.data.AudioResource;
import mro.fantasy.game.engine.communication.data.AudioResourceBundle;
import mro.fantasy.game.engine.communication.impl.AudioResourceMP3FileResolver;
import mro.fantasy.game.engine.plan.Plan;
import mro.fantasy.game.engine.plan.TileTemplate;
import mro.fantasy.game.resources.ResourceBundle;
import mro.fantasy.game.resources.impl.ClasspathResourceBundleProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class PlanConfiguration {

    @Bean
    public ClasspathResourceBundleProvider<AudioResource, AudioResourceBundle> getAudioResourceProvider() {
        return new ClasspathResourceBundleProvider<AudioResource, AudioResourceBundle>("mp3",
                (res) -> new AudioResourceBundle(res,
                        new AudioResourceMP3FileResolver.ClasspathMP3FileResolver("/mp3/data/" + AudioResourceBundle.normalizeBundleName(res.getFilename()))));
    }

    @Bean
    public ClasspathResourceBundleProvider<TileTemplate, ResourceBundle<TileTemplate>> getTileProvider() {
        return ClasspathResourceBundleProvider.forDefaultResourceBundle("tiles");
    }

    @Bean
    public ClasspathResourceBundleProvider<Plan, ResourceBundle<Plan>> getPlanProvider() {
        return ClasspathResourceBundleProvider.forDefaultResourceBundle("plan");
    }

}

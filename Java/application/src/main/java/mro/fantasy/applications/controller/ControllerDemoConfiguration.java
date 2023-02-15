package mro.fantasy.applications.controller;

import mro.fantasy.game.communication.impl.AudioResource;
import mro.fantasy.game.communication.impl.AudioResourceBundle;
import mro.fantasy.game.communication.impl.AudioResourceMP3FileResolver;
import mro.fantasy.game.plan.Plan;
import mro.fantasy.game.plan.TileTemplate;
import mro.fantasy.game.resources.ResourceBundle;
import mro.fantasy.game.resources.impl.ClasspathResourceBundleProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ControllerDemoConfiguration {

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

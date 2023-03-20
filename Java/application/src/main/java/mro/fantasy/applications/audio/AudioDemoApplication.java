package mro.fantasy.applications.audio;

import mro.fantasy.game.communication.AudioCommunicationService;
import mro.fantasy.game.communication.impl.AudioCommunicationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;

import java.util.Locale;

@SpringBootConfiguration
@ComponentScan({"mro.fantasy.game", "mro.fantasy.applications.plan"})
public class AudioDemoApplication implements CommandLineRunner {

    @Autowired
    private AudioCommunicationServiceImpl service;

    public static void main(String[] args) {
        SpringApplication.run(AudioDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        service.loadResources();
        service.playSync("Board Setup", "board.setup.tutorial.1", Locale.ENGLISH);
        // service.playSync("Example", "welcome", Locale.ENGLISH, new AudioCommunicationService.AudioVariable() {
        //     @Override
        //     public String getAudioBundle() {
        //         return "Character Names";
        //     }
        //
        //     @Override
        //     public String getAudioKey() {
        //         return "riskar";
        //     }
        // });
    }
}

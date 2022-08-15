package mro.fantasy.game.engine.communication.data;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.TextType;
import com.amazonaws.services.polly.model.VoiceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Support service to generate MP3 files from a resource bundle file with the help of the AWS Polly service.
 *
 * @author Michael Rodenbuecher
 * @since 2022-01-01
 */
public class AWSSpeechSynthesizer {

    /**
     * Logger
     */
    public static final Logger LOG = LoggerFactory.getLogger(AWSSpeechSynthesizer.class);

    /**
     * The amazon polly client to use
     */
    private AmazonPolly client;

    /**
     * Builder for the polly client.
     */
    private final AmazonPollyClientBuilder builder;

    /**
     * Creates a new synthesizer instance. This class expects an aws.config file to be present in the root of the classpath to read the required AWS credentials and configuration
     * values to run the synthesize process through the AWS Polly service. The file is a regular properties file with the following entries
     * <pre>
     *  region=eu-central-1
     *  accessKey=****
     *  secretKey=****
     * </pre>
     */
    public AWSSpeechSynthesizer() {

        Properties prop = new Properties();

        try {
            prop.load(getClass().getResourceAsStream("/aws.config"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot load polly.properties file. Speech synthesizing is disabled.", e);
        }

        Optional.ofNullable(prop.get("accessKey")).orElseThrow(() -> new IllegalArgumentException("No access key is set, speech synthesizing is disabled"));
        Optional.ofNullable(prop.get("secretKey")).orElseThrow(() -> new IllegalArgumentException("No secret key is set, speech synthesizing is disabled"));
        Optional.ofNullable(prop.get("region")).orElseThrow(() -> new IllegalArgumentException("No region is set, speech synthesizing is disabled"));

        this.builder = AmazonPollyClientBuilder.standard();
        builder.setRegion(prop.getProperty("region"));
        builder.setCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(prop.getProperty("accessKey"), prop.getProperty("secretKey"))));
        client = builder.build();
    }

    /**
     * Shutdown the polly client and release all resources.
     */
    public void close() {
        if (client != null) {
            client.shutdown();
            client = null;
        }
    }

    /**
     * Tries to synthesize the MP3 audio files for the passed resource bundle.
     * <p>
     * To save resources (either money or processing time) the AWS service is only triggered if the passed filter method returns {@code true}.
     *
     * @param voiceId        the AWS Polly voice ID to synthesize the speech. The voice ID is bound to a target language, i.e. the language of the passed input stream  and the
     *                       voice ID should match to achieve the best possible results...or you want to have funny results which is fine, too :).
     * @param resourceBundle the resource bundle to synthesize
     * @param filter         function to check if the audio resource should be synthesized or not
     * @param resultHandler  consumer to handle the result of the synthesize process. The consumer will receive the
     *
     * @throws IllegalArgumentException in case the YAML file is incorrect or a synthesizer error occurs.
     * @see <a href="https://docs.aws.amazon.com/polly/latest/dg/voicelist.html">AWS Polly Voices</a>
     */
    public void synthesize(VoiceId voiceId, AudioResourceBundle resourceBundle, Function<AudioResource, Boolean> filter, BiConsumer<AudioResource, InputStream> resultHandler) {

        Objects.requireNonNull(voiceId, "The voiceId parameter cannot be null.");
        Objects.requireNonNull(resourceBundle, "The resourceBundle parameter cannot be null.");
        Objects.requireNonNull(filter, "The filter parameter cannot be null.");
        Objects.requireNonNull(resultHandler, "The resultHandler parameter cannot be null.");

        if (client == null) {               // if the client was shut down we need to create a new one.
            client = builder.build();
        }

        resourceBundle.getAll().stream()
                .filter(filter::apply)               // check if the audio resource was modified
                .forEach(res -> {                               // in this case we need to synthesize the file with the AWS Polly service.

                    LOG.debug("Resource ::= [{}] was modified or is new", res);

                    // Each resource can be a multipart resource, i.e. we may need to synthesize multiple MP3 streams for a single resource. Since this works for simple (without
                    // placeholder) as well, we can handle them like multipart ones.
                    // The split audio resource needs to be filtered for placeholder streams, which should not be synthesizes (they are replaced with other audio resources when
                    // the concatenated MP3 stream is generated in the getMP3Stream(...) method of the resource.

                    res.split().stream()
                            .filter(part -> !part.isVariablePlaceholder())
                            .forEach(part -> resultHandler.accept(part, synthesizeSpeech(voiceId, part.getSSML())));

                });
    }

    /**
     * Synthesizes the given text and returns the stream of the generated mp3 file.
     *
     * @param voiceId the AWS Polly voice to synthesize the text
     * @param text    the text to synthesize
     *
     * @return the input stream with the synthesized text in the MP3 format
     */
    private InputStream synthesizeSpeech(VoiceId voiceId, String text) {
        try {
            LOG.debug("Synthesize speech, voice ::= [{}], text ::= [{}]", voiceId, text);

            SynthesizeSpeechRequest synthesizeSpeechRequest = new SynthesizeSpeechRequest()
                    .withOutputFormat(OutputFormat.Mp3)
                    .withVoiceId(voiceId)
                    .withText(text)
                    .withTextType(TextType.Ssml)
                    .withEngine("neural");

            return client.synthesizeSpeech(synthesizeSpeechRequest).getAudioStream();

        } catch (Exception e) {
            LOG.error("Cannot create file stream: ", e);
            throw new IllegalArgumentException(e);
        }
    }

}

package mro.fantasy.game.communication.impl;

import com.amazonaws.services.polly.model.VoiceId;
import mro.fantasy.game.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Locale;

/**
 * Utility class to manage audio files for the game system.
 * <p>
 * The game system interaction with the player is done primary over audio announcement which have to be present in the form of MP3 files. In general, it would be sufficient to
 * simply provide these MP3 files so that they can be played back to the player. However, during development the texts may change on a regular basis and if the developers would be
 * forced to generate these files by their own this would disrupt the development process. To make the process easier the communication system is therefore based on SSML (Speech
 * Syntax Markup Language) resources which define the audio content that is played back to the player. To make the development process as immersive as possible these SSML resources
 * can be automatically synthesized to MP3 files with the help of services which are available on the internet.
 * <p>
 * As a result this class can be used to synthesize MP3 files with the help of the AWS Polly service. The developer has to provide the audio resources as YAML files in a specific
 * format that form an {@link AudioResourceBundle} (for the YAML format see that class). A resource bundle is a collection of multiple SSML snippets that are grouped together in a
 * single file. The number of these files and their game related content is up to the developer and not part of this library. While the resource bundle contains all textual
 * information about the audio resources the actual MP3 files are manages separately. The reason for this separation is the way in which the data is managed and delivered as part
 * of the game system.
 * <p>
 * The files are generated by this utility class in the context of the OS file system within a defined directory. Afterwards they can be picked up by the build process and be added
 * to a JAR file to access them via the classpath. Other mechanisms are possible of course.
 * <p>
 * As a result the developer only needs to modify the SSML parts of the resource bundle files to regenerate NP3 files.
 * <p>
 * This class expects an aws.config file to be present in the root of the classpath to read the required AWS credentials and configuration values to run the synthesize process
 * through the AWS Polly service. The file is a regular properties file with the following entries
 * <pre>
 *   region=eu-central-1
 *   accessKey=****
 *   secretKey=****
 * </pre>
 *
 * @author Michael Rodenbuecher
 * @see AWSSpeechSynthesizer
 * @since 2022-07-20
 */
public final class AudioResourceFileManager {

    /**
     * Logger
     */
    public static final Logger LOG = LoggerFactory.getLogger(AudioResourceFileManager.class);

    /**
     * Synthesizes the MP3 files for the passed resource file that has the correct YAML format (see {@link AudioResourceBundle}. The process will generate missing MP3 files
     * relative to the path of the passed resource file in a directory <code>/data/{normalizedBundleName}</code>.The normalized bundle name that is defined in the YAML file (see
     * {@link AudioResourceBundle#normalizeBundleName(String)}.
     * <p>
     * To save time and money (the AWS Polly Service is not free) the files are only synthesized if they either have changed, i.e. the {@link AudioResource#isModified()} method
     * returns <code>true</code> or if the resource has no representing file in the target directory.
     * <p>
     * In addition a cleanup is run after the synthesize process that removes all MP3 files in the target directory that have no corresponding entry in the resource file.
     *
     * @param resourceFile the YAML file to synthesize
     *
     * @throws IllegalArgumentException in case the synthesize process fails. This will only be thrown in case of major errors. If a single file cannot be synthesized the process
     *                                  will continue. Please check the logs in that case
     */
    public static void synthesize(Path resourceFile) {

        LOG.debug("Synthesize resource file: {}", resourceFile);

        ValidationUtils.requireTrue(resourceFile.toFile().exists(), "A file ::= [" + resourceFile + "] does not exist");

        Path parent = resourceFile.getParent();

        ValidationUtils.requireNonNull(parent, "Cannot resolve parent of ::= [" + resourceFile + "]");

        // first we try to load the audio resources from the passed file, we do not need an MP3 resolver here since we do not want to play back these resources atm
        AudioResourceBundle resourceBundle;
        try {
            resourceBundle = new AudioResourceBundle(new FileInputStream(resourceFile.toFile()), null);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not synthesize ::= [" + resourceFile + "]: ", e);
        }

        var targetDirectory = Path.of(parent.toString(), "data", AudioResourceBundle.normalizeBundleName(resourceFile.getFileName().toString()));

        LOG.debug("Try to synthesize MP3 files for ::= [{}] to target directory ::= [{}]", resourceFile, targetDirectory);

        synthesizeMP3Files(resourceBundle, targetDirectory);
        updateYAMLFile(resourceBundle, resourceFile);

    }

    /**
     * Synthesizes the MP3 files for the passed resource bundle to the target directory.
     *
     * @param resourceBundle  the resource bundle to synthesize.
     * @param targetDirectory the directory where the MP3 files are stored
     */
    private static void synthesizeMP3Files(AudioResourceBundle resourceBundle, Path targetDirectory) {

        var speechSynthesizer = new AWSSpeechSynthesizer();

        // if the target directory does not exist already we need to create it
        if (!targetDirectory.toFile().exists()) {
            LOG.debug("Create target directory ::= [{}]", targetDirectory);
            targetDirectory.toFile().mkdirs();
        }

        speechSynthesizer.synthesize(
                resolveVoiceId(resourceBundle.getLocale()),
                resourceBundle,
                // filter function to decide if the audio resource should be rendered or not. If the content of the YAML file was modified, we need to regenerate the file,
                // same if the target location does not have a MP3 file already.
                res -> res.isModified() || !res.getMP3FileName(targetDirectory).toFile().exists(),
                // consumer of synthesized resources, if the storing procedure fails we still continue. If a file is already in place it will be replaced by the new input
                // stream, i.e. if the filter returns true existing files will be overwritten.
                (res, inStrm) -> {
                    try {
                        Files.copy(inStrm, res.getMP3FileName(targetDirectory), StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception e) {
                        LOG.warn("Cannot synthesize resource ::= [{}]: ", res, e);
                    }
                }
        );

        speechSynthesizer.close();
    }

    /**
     * Performs an update of the YAML file that was used to generate the MP3 files. Inside the YAML file a hash value for every SSML definition is stored along with the key. If
     * this hash has changed this class uses that as a trigger to regenerate the underlying MP3 file. This can be used by the developer to change the SSML without telling this
     * class which resources have to be regenerated.
     * <p>
     * In his daily work that means that the developer can simply modify the SSML and this utility class will recognize that the SSML hash does not match the hash stored in the
     * bundle. As a result the existing MP3 file will be replaced by the new version of the MP3. This is exactly the same for new resources, i.e. if the hash entry in the bundle is
     * empty a generation will happen.
     * <p>
     * To avoid the regeneration with every run the hash in the bundle needs to be updated after the creation of a MP3 field which will be performed by this method.
     *
     * @param bundle       the bundle for which the MP3s were generated
     * @param resourceFile the YAML file from which the bundle was created
     */
    private static void updateYAMLFile(AudioResourceBundle bundle, Path resourceFile) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(resourceFile, StandardOpenOption.TRUNCATE_EXISTING);
            bundle.writeYAML(writer);
            writer.close();
            LOG.debug("Updated resource YAML file ::= [{}]", resourceFile);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not update resource YAML file ::= [" + resourceFile + "]", e);
        }
    }

    /**
     * Resolves a locale to an AWS Polly voice ID
     *
     * @param locale the locale
     *
     * @return the voice ID
     *
     * @throws IllegalArgumentException if no voice ID is available for the passed locale
     */
    private static VoiceId resolveVoiceId(Locale locale) {
        switch (locale.getLanguage()) {
            case "en":
                return VoiceId.Matthew;
            case "de":
                return VoiceId.Vicki;
            default:
                throw new IllegalArgumentException("Cannot find an AWS Polly voice for locale ::= [" + locale + "]");
        }
    }

    /**
     * Tales multiple YAML files which follow the resource bundle definition in {@link AudioResourceBundle} and synthesizes the corresponding MP3 files. You can use this main
     * method in combination with the Maven execute plugin to generate missing MP3 files during the build process and cleanup files which are not described in the YAML anymore.
     * <p>
     * To make this tool work in the best way together with the default implementation of the {@link AudioResourceBundle} you should put your YAML files into the root of the JAR
     * file into the folder <code>mp3</code> which corresponds to the <code>>${project.basedir}/src/main/resources/mp3</code> directory in your maven project. If you generate the
     * MP3 files in this way and add the resulting JAR to the classpath of a game application, the audio resources will be made available automatically without additional
     * configuration effort.
     * <p>
     * Please keep in mind that you need an aws.config file in the classpath of the build process with the credentials to access the AWS Polly service (@see {@link
     * AudioResourceFileManager}).
     *
     * <p>
     * <b>Example Maven Configuration</b>
     *
     * <pre>{@code
     *  <build>
     *     <plugins>
     *       <plugin>
     *         <groupId>org.codehaus.mojo</groupId>
     *         <artifactId>exec-maven-plugin</artifactId>
     *         <version>3.1.0</version>
     *         <executions>
     *           <execution>
     *             <phase>package</phase>
     *             <goals>
     *               <goal>java</goal>
     *             </goals>
     *           </execution>
     *         </executions>
     *         <configuration>
     *           <mainClass>mro.fantasy.game.engine.communication.impl.AudioResourceFileManager</mainClass>
     *           <arguments>
     *             <argument>${project.basedir}/src/main/resources/mp3/example_en.yaml</argument>
     *           </arguments>
     *         </configuration>
     *       </plugin>
     *     </plugins>
     *   </build>
     * }</pre>
     *
     * @param args
     */
    public static void main(String[] args) {

        Arrays.stream(args).forEach(resPath -> AudioResourceFileManager.synthesize(Path.of(resPath)));


    }


}

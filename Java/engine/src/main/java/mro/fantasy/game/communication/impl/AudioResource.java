package mro.fantasy.game.communication.impl;

import mro.fantasy.game.engine.GameLibrary;
import mro.fantasy.game.resources.GameResource;
import mro.fantasy.game.utils.Hash;
import mro.fantasy.game.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Communication resource to inform the player about game state. The resource itself is usually backed by an MP3 file that can be played to inform the player about the current game
 * state or give instructions.
 *
 * @author Michael Rodenuecher
 * @since 2021-12-31
 */
public class AudioResource implements GameResource {

    /**
     * Logger
     */
    public static final Logger LOG = LoggerFactory.getLogger(AudioResource.class);

    private static final String SPEAK_OPEN = "<speak>";
    private static final String SPEAK_CLOSE = "</speak>";

    /**
     * Placeholder for variables in a multi part String.
     */
    private static final AudioResource VAR_PLACEHOLDER = new AudioResource();

    /**
     * The resource bundle where the resource is managed.
     */
    private final AudioResourceBundle bundle;

    /**
     * The unique key of the resource which need to be unique in combination with the class.
     */
    private final String key;

    /**
     * The actual text.
     */
    private final String ssml;

    /**
     * The hashed text.
     */
    private final String hash;

    /**
     * The hash from the resource bundle.
     */
    private final String bundleHash;

    /**
     * The function to resolve the underlying MP3 stream for a resource.
     */
    private final AudioResourceMP3FileResolver mp3StreamFunction;

    /**
     * Private constructor for the {@link #VAR_PLACEHOLDER} field in this class. Please do NOT use or change this constructor since some methods in this class ({@link #split()},
     * {@link #getMP3Stream}, etc. ) depend on the special placeholder that is identifies as a resource with a key that has the value {@code null} which cannot be created with
     * other methods or constructors.
     */
    private AudioResource() {
        this.bundle = null;
        this.key = null;
        this.ssml = null;
        this.hash = null;
        this.bundleHash = null;
        this.mp3StreamFunction = null;
    }

    /**
     * Creates a new resource.
     *
     * @param bundle            The resource bundle where the communication resource is managed.
     * @param key               The unique key of the resource which need to be unique in combination with the class.
     * @param ssml              The actual text in SSML format; inside the text variables can be used which have to be provided when the MP3 streams are constructed. To be valid
     *                          the SSML needs to start and end with the &lt;speak&gt; tag. If this tag is missing it will be added automatically during the construction. To do
     *                          this trailing and leading whitespaces are removed.
     * @param bundleHash        The hash from the resource bundle.
     * @param mp3StreamFunction the function to resolve the underlying MP3 stream for a resource; the input parameter is the key of the resource that should be con, the path part
     *                          depends on the bundle that was used to create the function
     *
     * @throws NullPointerException if one of the passed parameter (beside the bundleHash) is {@code null}
     */
    protected AudioResource(AudioResourceBundle bundle, String key, String ssml, String bundleHash, AudioResourceMP3FileResolver mp3StreamFunction) {

        ValidationUtils.requireNonNull(bundle, "The passed bundle cannot be null.");
        ValidationUtils.requireNonNull(key, "The passed key cannot be null.");
        ValidationUtils.requireNonNull(ssml, "The passed ssml cannot be null.");
        ValidationUtils.requireNonNull(mp3StreamFunction, "The passed mp3StreamFunction cannot be null.");

        this.bundle = bundle;
        this.key = key;
        this.bundleHash = bundleHash;
        this.mp3StreamFunction = mp3StreamFunction;

        String tmpSSML = ssml.stripTrailing().stripLeading().replaceAll(" +", " ");           // normalize the SSML here. This is needed for the operations in the split method.

        if (!tmpSSML.startsWith(SPEAK_OPEN)) {                           // to be valid from AWS perspective every SSML text needs to start with the <speak> tag
            tmpSSML = SPEAK_OPEN + tmpSSML;
        }

        if (!tmpSSML.endsWith(SPEAK_CLOSE)) {                            // and is closed.
            tmpSSML += SPEAK_CLOSE;
        }

        this.ssml = tmpSSML;
        this.hash = Hash.hash(ssml, "[\\s\\t(\\r?\\n)]+");
    }

    @Override
    public void loadFromYAML(GameLibrary library, Map<String, Object> data) {
        // not needed here.
    }

    // region Setter and Getter
    // ------------------------------------------------------------------------------------------------------------------------

    /**
     * Returns the resource bundle where the resource is used.
     *
     * @return the bundle
     */
    public AudioResourceBundle getBundle() {
        return bundle;
    }

    /**
     * Returns the unique key of the resource which need to be unique in combination with the class.
     *
     * @return the key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the text in the given locale for the resource.
     *
     * @return the text
     */
    public String getSSML() {
        return ssml;
    }

    /**
     * Returns the locale of this resource
     *
     * @return the locale
     */
    public Locale getLocale() {
        return getBundle().getLocale();
    }

    /**
     * Checks if this audio resource is a special variable placeholder one. This special resources are used when MP3 streams are constructed from audio resources which have
     * variable placeholder inside. When a resource is {@link #split()} the placeholder indicate where variables have to be placed when the {@link #getMP3Stream(AudioResource...)}
     * method is called.
     *
     * @return {@code true} if this is a variable placeholder, {@code false} otherwise
     */
    public boolean isVariablePlaceholder() {
        return key == null; // only possible when the special private constructor is used.
    }

    /**
     * Returns the file name of this resource. The file name does NOT contain any path information but is just the plain name without any location information, i.e. this method
     * cannot be used to load the file afterwards. To get access to the underlying MP3 stream use the {@link #getMP3Stream(AudioResource...)} method instead.
     * <p>
     * If this resource {@link #hasVariables()} the returned path will only be the first part of the resource audio files, i.e. with the suffix #1.
     *
     * @return the file name
     */
    public String getMP3FileName() {
        if (hasVariables()) {
            return key.toLowerCase(Locale.ROOT).replace(".", "_") + "#1_" + getLocale().getLanguage().toLowerCase(Locale.ROOT) + ".mp3";
        } else {
            return key.toLowerCase(Locale.ROOT).replace(".", "_") + "_" + getLocale().getLanguage().toLowerCase(Locale.ROOT) + ".mp3";
        }
    }

    /**
     * Returns a path for this resource based on the passed target directory and the result of {@link #getMP3FileName()} method
     *
     * @param targetDirectory the directory part of the filename
     *
     * @return the file name
     */
    public Path getMP3FileName(Path targetDirectory) {
        return Path.of(targetDirectory.toString(), getMP3FileName());
    }

    /**
     * Checks if the resource has variables, i.e. if multiple MP3 files are used to construct the actual MP3 stream.
     *
     * @return {@code true} if the resource has variables, {@code false} otherwise.
     */
    public boolean hasVariables() {
        return ssml.contains("{}");
    }

    /**
     * Checks if the bundle hash code matches the current one.
     *
     * @return {@code false} if the text of this resource is the same, {@code false} otherwise
     */
    public boolean isModified() {
        return !hash.equals(bundleHash);
    }

    // endregion

    /**
     * Returns the input stream which represent the communication resource. This method returns a single stream, if the SSML has variables in it, they will be resolved to MP3
     * streams, too, and concatenated to each other. This will allow the {@link AudioResourcePlayTask} to play back the complete resource with all variables in one stream.
     * <p>
     * It is not allowed to pass audio resource variables which need variables on their own.
     *
     * @param variables the (optional) variables used by this resource.
     *
     * @return the list od MP3 streams representing this resource
     */
    public InputStream getMP3Stream(AudioResource... variables) {

        if (!hasVariables()) {                                         // if no variables are present in the SSML we can
            return mp3StreamFunction.resolve(this);        // simply return the single MP3 file that is represented
        }                                                              // by this resource.

        ValidationUtils.requireFalse(
                variables == null || variables.length != ssml.split("\\{\\}", -1).length - 1,    // count the occurrence of {} substring in SSML text
                "The passed number of variables does not match the number of placeholder in the underlying SSML");

        if (variables != null) {
            Arrays.stream(variables).forEach(variable -> ValidationUtils.requireFalse(variable.hasVariables(),
                    "A variable audio resource cannot have own variables"));
        }

        InputStream resultStream = null;       // the concatenated input stream that is constructed
        InputStream tmpStream;                 // temporary stream used in the loop to store the next handled stream
        int placeholderCnt = 0;                // counter for the placeholder


        for (AudioResource part : split()) {   // split the SSML of this resource into multiple audio resources

            if (part.isVariablePlaceholder()) tmpStream = variables[placeholderCnt++].getMP3Stream();  // NOSONAR variables is null checked above
            else tmpStream = part.getMP3Stream();                                                      // no variables allowed here !

            if (resultStream == null) resultStream = tmpStream;                     // if the first stream is constructed we need to initialize the result stream
            else resultStream = new SequenceInputStream(resultStream, tmpStream);   // otherwise, we concatenate the streams to a sequence

        }
        return resultStream;

    }

    /**
     * If this resource is a multipart resource ({@link #hasVariables()} returns {@code true}), i.e. the SSML definition contains one or more variable placeholder, this method will
     * split up the resource into multiple audio resources.
     * <p>
     * In this case there will be no single MP3 file for the resource itself but multiple files with an index. This is the way who such a resource has to be stored. For example, a
     * resource with key foo (locale english) that has an SSML definition of
     *
     * <speak>Hello {}, how are you ?</speak>
     * <p>
     * would be presented by two files with the name foo_en#1.mp3 and foo_en#2.mp3.
     * <p>
     * If no variables are set the returned list will contain exactly one entry which represents the MP3 file for this resource. The returned list will contain special {@link
     * #VAR_PLACEHOLDER} resources that can be used to replace these placeholders with the correct content of the variables. These placeholders, of course, do not have an index or
     * even a key, so be careful and do not use them. You can check if an audio resource is the special placeholder one by calling the {@link #isVariablePlaceholder()} method.
     *
     * @return the audio resources
     */
    public List<AudioResource> split() {

        if (!hasVariables()) {              // if no variables are present in the SSML we can
            return List.of(this);           // simply return the single MP3 file that is represented
        }                                   // by this resource.

        // first we split the SSML at the variable locations which are marked with {}, this will not take into account scenarios where a variable is at the beginning or the end
        // of an SSML definition but since every valid text has to start with a <speak> tag and end with a </speak> tag it wouldn't be valid anyway. In that edge case it is
        // possible that empty MP3 files are generated but that can be filtered afterwards.

        var sequence = new AtomicInteger(0);                  // sequence number of the parts to create unique keys. Use AtomicInteger due to lambda final constraint

        return Arrays.stream(getSSML().split("((?=\\{\\})|(?<=\\{\\}))"))
                .sequential()                                                    // processing has to be sequential to preserve the order of the audio stream
                .filter(part -> !isEmptySSML(part))                              // empty parts (only with <speak> tags) can be skipped
                .map(part -> {
                    if (part.equals("{}")) {                                     // if the part is a placeholder we return the special placeholder resource.
                        return VAR_PLACEHOLDER;
                    } else {
                        return new AudioResource(bundle,                         // otherwise, we create a new AudioResource object from the part
                                getKey() + "#" + (sequence.incrementAndGet()),   // with a unique key that is increased for every part of the original SSML
                                part,                                            // the new SSML, <speak> tags will be added automatically by the constructor.
                                null,
                                mp3StreamFunction);
                    }
                })
                .toList();

    }

    /**
     * Converts the resource into a map in YAML format as described in {@link AudioResourceBundle}.
     *
     * @return the YAML map
     */
    public Map<String, Object> toYAMLMap() {
        var resource = new LinkedHashMap<String, Object>(); // use a linked hashmap here to preserve the order of the attributes as defined in the example
        resource.put("key", this.key);
        resource.put("hash", this.hash);
        resource.put("ssml", this.ssml);

        return resource;
    }

    @Override
    public String toString() {
        return "CommunicationResource{" +
                       "bundle=" + bundle +
                       ", key='" + key + '\'' +
                       ", ssml='" + (ssml != null ? ssml.replace("\n", "") + '\'' : null) +
                       ", hash='" + hash + '\'' +
                       ", bundleHash='" + bundleHash + '\'' +
                       '}';
    }

    // region Utility functions
    // ------------------------------------------------------------------------------------------------------------------------

    /**
     * Checks if the SSML is empty, i.e. if it only contains the starting and ending speak tag.
     *
     * @return {@code true} if the SSML is empty, {@code false} otherwise.
     */
    private static boolean isEmptySSML(String ssml) {
        String tmpSSML = ssml.replace(" ", "");

        return tmpSSML.equals(SPEAK_OPEN + SPEAK_CLOSE)
                       || tmpSSML.equals(SPEAK_OPEN)
                       || tmpSSML.equals(SPEAK_CLOSE);
    }


    /**
     * Creates a new audio resource from the YAML format specified in {@link AudioResourceBundle}.
     * <p>
     * <b>Example map entry:</b>
     * <pre>{@code
     *    - key: player.1x1
     *      hash: 7750d93d025e9b68ef6010ec09542740ff0fa1acf1e21a04c4d2fbe772bb625e
     *      text: |
     *        <speak>
     *          <say-as interpret-as="digits">001</say-as>
     *        </speak>
     *   }</pre>
     *
     * @param bundle            the bundle to which the resource belongs
     * @param resource          the actual data
     * @param mp3StreamFunction the mp3 resolver to generate the correct input streams for the {@link #getMP3Stream(AudioResource...)} method
     *
     * @return the new resource
     *
     * @throws IllegalArgumentException in case mandatory data is missing in the yaml map
     */
    public static AudioResource fromYAMLMap(AudioResourceBundle bundle, Map<String, Object> resource, AudioResourceMP3FileResolver mp3StreamFunction) {

        var cRes = new AudioResource(
                bundle,
                (String) Optional.ofNullable(resource.get("key")).orElseThrow(() -> new IllegalArgumentException(String.format("A key field is missing for entry ::= [%s]", resource))),
                (String) Optional.ofNullable(resource.get("ssml")).orElseThrow(() -> new IllegalArgumentException(String.format("A ssml field is missing for entry ::= [%s]", resource))),
                (String) resource.get("hash"),
                mp3StreamFunction);

        LOG.trace("Created new audio resource: {}", cRes);

        return cRes;
    }

    @Override
    public String getName() {
        return getKey();
    }

    @Override
    public String getDescription() {
        return getSSML();
    }

    @Override
    public String getGameId() {
        return getKey();
    }

    // endregion


}

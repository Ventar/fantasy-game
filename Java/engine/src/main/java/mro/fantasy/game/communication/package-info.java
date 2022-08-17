/**
 * The communication package is responsible for the audio interaction with the player.
 * <p>
 * While the internal architecture of the communication package follows the one described for resource bundles ({@link mro.fantasy.game.resources}), the communication service has
 * some extensions that allow the playback of MP3 files. The {@link mro.fantasy.game.communication.impl.AudioResourceBundle} contains {@link
 * mro.fantasy.game.communication.impl.AudioResource}s with a key ({@link mro.fantasy.game.resources.GameResource#getGameId()} and SSML (Speech Syntax Markup language) that allows
 * the generation of audio MP3 or WAV files through a Text-To-Speech engine.
 * <p>
 * <img src="doc-files/AudioResources.png"/>
 * <h2>Synthesize Speech</h2>
 * The process of synthesizing speech is done during the development phase of the game. The YAML resource files are parsed during the build process and the Amazon Polly service is
 * used to generate the MP3 files which are stored in the resource folder of the maven project. The process is triggered by the {@link
 * mro.fantasy.game.communication.impl.AudioResourceFileManager#main(java.lang.String[])}  method
 * <ul>
 *     <li>that loads the YAML file</li>
 *     <li>parses the SSML</li>
 *     <li>send the SSML to the AWS service</li>
 *     <li>and stores the returned MP3 file</li>
 * </ul>
 * The service is not free and needs a valid api key and secret that has to be configured in a {@code aws.config} file that is available through the classpath. An example file
 * is available in the resources folder of the maven project. These MP3 files are currently stored in the JAR file and commited into the cloud. This may change in the future due
 * to the problem GIT has with binary files.
 * <h2>Audio Playback</h2>
 * Every audio resource that is imported as part of the {@link mro.fantasy.game.resources.ResourceBundle#loadResources(java.util.function.Function)} method has to have a valid
 * MP3 file in the classpath so that the {@link mro.fantasy.game.communication.impl.AudioResourceMP3FileResolver.ClasspathMP3FileResolver} can load the file during runtime and
 * pass it to an {@link mro.fantasy.game.communication.impl.AudioResourcePlayTask} that is responsible for the playback of the audio file.
 * <p>
 * If the complete default configuration is used, the valid YAML files should reside in the {@code resources/mp3} folder, will be picked up during the build process and
 * are stored in the {@code resources/mp3/data/{yamlname}/} folder.
 * <p>
 *  * <img src="doc-files/CreateMP3Files.png"/>
 *
 * @see mro.fantasy.game.resources
 */
package mro.fantasy.game.communication;
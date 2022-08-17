package mro.fantasy.game.communication.impl;

import javazoom.jl.player.Player;
import mro.fantasy.game.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task to play an audio stream to the player using an {@link Player}.
 *
 * @author Michael Rodenbuecher
 * @since 2022-07-21
 */
public class AudioResourcePlayTask implements Runnable {

    /**
     * Logger
     */
    public static final Logger LOG = LoggerFactory.getLogger(AudioResourcePlayTask.class);

    /**
     * Information about current state of the task.
     */
    public enum State {
        QUEUED,
        PLAYING,
        STOPPED
    }

    /**
     * The audio resource that should be played by this task.
     */
    private AudioResource resource;

    /**
     * Additional variables to execute {@link AudioResource#getMP3Stream(AudioResource...)}
     */
    private AudioResource[] variables;

    /**
     * The current state of the player
     */
    private State state = State.QUEUED;

    /**
     * The actual player object.
     */
    private Player player;

    /**
     * Creates a new task to play. The MP3 stream for this task is generated before it is played back to the player, this will safe resources in case the playback is stopped before
     * it started.
     *
     * @param resource  the resource to play.
     * @param variables additional variables to execute {@link AudioResource#getMP3Stream(AudioResource...)}
     *
     * @throws NullPointerException if the passed resource is <code>null</code>.
     */
    public AudioResourcePlayTask(AudioResource resource, AudioResource... variables) {
        ValidationUtils.requireNonNull(resource, "there must be at least one audio resource to be played.");
        this.resource = resource;
        this.variables = variables;
    }

    /**
     * Stops the current running player.
     */
    public void stop() {
        state = State.STOPPED;
        if (player != null) {
            LOG.debug("Stop audio playback of ::= [{}]", resource);
            player.close();
        }
    }

    /**
     * Returns the current state of the task.
     *
     * @return the current state
     */
    public State getState() {
        return state;
    }

    @Override
    public void run() {
        try {

            if (state == State.STOPPED) {     // If the task was stopped before it was started we can simply return here.
                return;
            }

            player = new Player(resource.getMP3Stream(variables), javazoom.jl.player.FactoryRegistry.systemRegistry().createAudioDevice());

            state = State.PLAYING;            // otherwise, we change the state to playing.

            player.play();                    // blocking operation
            player.close();                   // will close the input stream

            state = State.STOPPED;

        } catch (Exception e) {
            LOG.error("Cannot play audio stream: ", e);
        }
    }
}

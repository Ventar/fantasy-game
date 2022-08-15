package mro.fantasy.game.engine.communication.impl;

import mro.fantasy.game.engine.GameLibrary;
import mro.fantasy.game.resources.GameResource;
import mro.fantasy.game.resources.impl.AbstractGameResource;
import mro.fantasy.game.utils.YAMLUtilities;
import mro.fantasy.game.engine.communication.AudioCommunicationService;

import java.util.Map;

/**
 * A game resource which can be used by the {@link mro.fantasy.game.engine.communication.AudioCommunicationService} to synthesize MP3 output.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-06
 */
public abstract class AbstractAudioGameResource extends AbstractGameResource implements AudioCommunicationService.AudioVariable {

    /**
     * Returns the name of the audio bundle to use this tile template with the {@link AudioCommunicationService}
     *
     * @see #getAudioBundle()
     */
    protected String audioBundleName;

    /**
     * Returns the name of the audio key to use this tile template with the {@link AudioCommunicationService}
     *
     * @see #getAudioBundle()
     */
    protected String audioKey;

    /**
     * Loads the needed fields from the YAML map. This part of the parsing process loads the {@link #getAudioBundle()} and {@link #getAudioKey()} part of the game resource in the
     * map.
     *
     * @param library the game library to resolve entities which are only referred by their {@link GameResource#getGameId()}.
     * @param data    the YAML data map.
     */
    public void loadFromYAML(GameLibrary library, Map<String, Object> data) {

        super.loadFromYAML(library, data);

        this.audioBundleName = YAMLUtilities.getMandatory(data, "audioBundleName");
        this.audioKey = YAMLUtilities.getMandatory(data, "audioKey");
    }

    @Override
    public String getAudioBundle() {
        return audioBundleName;
    }

    @Override
    public String getAudioKey() {
        return audioKey;
    }


    /**
     * Copies all data from this abstract resource to the passed resource.
     *
     * @param newResource the resource to fill with the data of this instance
     */
    protected void copy(AbstractAudioGameResource newResource) {
        super.copy(newResource);
        newResource.audioBundleName = audioBundleName;
        newResource.audioKey = audioKey;
    }

}

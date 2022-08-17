package mro.fantasy.game.engine.character;

import mro.fantasy.game.resources.GameResource;
import mro.fantasy.game.communication.AudioCommunicationService;
import mro.fantasy.game.engine.gamedata.GameDataSet;

/**
 * Represents a character within the game system. A character can either be a player character, i.e. it is controlled manually by a human player, an AI controlled enemy character
 * or an AI controlled fellow.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-04
 */
public interface Character extends GameResource, AudioCommunicationService.AudioVariable {

    /**
     * Returns the current game data set of a character. The current values od a character can be fetched by this method will provide an aggregated view on the character.
     * Aggregated means that if a character with {@link mro.fantasy.game.engine.gamedata.GameDataType#STRENGTH} value of 5 has equipped a ring that increases the attribute by +2,
     * the returned data set would contain a value of 7.
     *
     * @return the game data set
     */
    GameDataSet getGameDataSet();

    /**
     * Adds a character extension to the character. This method does not check any prerequisites that have to be fulfilled to add the extension.
     *
     * @param extension the extension to add
     */
    void addCharacterExtension(CharacterExtension extension);

}

package mro.fantasy.game.engine.character;

import mro.fantasy.game.engine.action.Action;
import mro.fantasy.game.engine.character.skill.SkillType;
import mro.fantasy.game.resources.GameResource;
import mro.fantasy.game.engine.gamedata.GameDataSet;

import java.util.Collection;
import java.util.Map;

/**
 * A {@link Character} has some basic {@link mro.fantasy.game.engine.gamedata.GameDataType} values set which can be extended by these extensions. An extension is an abstract
 * concept and used to make the character generation as flexible as possible. Every extension has an
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-06
 */
public interface CharacterExtension extends GameResource {

    /**
     * Returns the type of the extension. Depending on the type this extension can be cast to the corresponding Java class like {@link
     * mro.fantasy.game.engine.character.equpipment.Equipment}.
     *
     * @return the type of the extension
     */
    CharacterExtensionType getExtensionType();

    /**
     * Returns a data set that contains the prerequisite values for any combination of {@link mro.fantasy.game.engine.gamedata.GameDataType}s that need to be fulfilled by the
     * {@link mro.fantasy.game.engine.character.Character} to use this extension. The current values of a character can be fetched by the {@link Character#getGameDataSet()}
     * method.
     * <p>
     * If the extension can be applied without any prerequisite this method will return a default game data set with all values set to zero, i.e. a character will always fulfill
     * the prerequisites.
     *
     * @return a game data set with the prerequisites.
     */
    GameDataSet getGameDataPrerequisites();

    /**
     * Returns a list of {@link CharacterExtensionType#SKILL}s a character needs to have to get this extension applied. The data is returned as a map with the {@link SkillType} as
     * key and the minimum skill value as value of the map.
     * <p>
     * If no skill prerequisites are needed for this extension an empty map is returned
     *
     * @return a map with skill types and their minimum values
     */
    Map<SkillType, Integer> getSkillPrerequisites();

    /**
     * Returns a gama data set with values that modifies the character on which the extension was applied.
     *
     * @return the modification set
     */
    GameDataSet getGameDataModification();

    /**
     * Returns a collection of actions that can be performed when this extension was applied to a character. If a character equips a one-handed weapon for example it would allow
     * him to attack somebody. An equipped spell would allow the character to cast that spell.
     *
     * @return possible actions.
     */
    Collection<Action> getActions();

}

package mro.fantasy.game.engine.character.effect;

import mro.fantasy.game.engine.character.CharacterExtension;

/**
 * A temporary (or permanent) effect that is applied to a character like poison, a blessing or a spell. An effect modifies the {@link mro.fantasy.game.engine.gamedata.GameDataSet}
 * of a character for a given period of time, i.e. the effect is applied to the character on every game turn until it ends.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-06
 */
public interface Effect extends CharacterExtension {



}

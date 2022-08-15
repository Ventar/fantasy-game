package mro.fantasy.game.engine.character;

/**
 * The type of a {@link CharacterExtension}.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-06
 */
public enum CharacterExtensionType {

    /**
     * Basic extensions provide actions to characters which are common and do not belong to any other type. The ability to move is an example for a basic extension type.
     */
    BASIC,

    /**
     * A skill that allows a character to perform special {@link mro.fantasy.game.engine.action.Action}, modifies the {@link mro.fantasy.game.engine.gamedata.GameDataSet} of the
     * character oder is a marker as a prerequisite for other extensions.
     */
    SKILL,

    /**
     * A spell that can be cast by a character.
     */
    SPELL,

    /**
     * Equipment that works like a {@link #SKILL}, i.e. add possible {@link mro.fantasy.game.engine.action.Action}s or change the {@link
     * mro.fantasy.game.engine.gamedata.GameDataSet} of the character. In contrast to a skill, equipment can be changed, which means the benefits (or drawbacks) of an equipment
     * extension is more flexible
     */
    EQUIPMENT,

    /**
     * A temporary (or permanent) effect that is applied to a character like poison, a blessing or a spell. An effect modifies the {@link
     * mro.fantasy.game.engine.gamedata.GameDataSet} of a character for a given period of time, i.e. the effect is applied to the character on every game turn until it ends.
     */
    EFFECT

}

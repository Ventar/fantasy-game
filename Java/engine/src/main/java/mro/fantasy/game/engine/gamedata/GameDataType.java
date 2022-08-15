package mro.fantasy.game.engine.gamedata;

/**
 * A set of data types which are used...and here we need more brainstorming...
 *
 * @author Michael Rodenbuecher
 * @since 2022-07-25
 */
public enum GameDataType {

    STRENGTH,           // Weapon heavy, Health Bonus
    STAMINA,            // Armor, Endurance Bonus
    AGILITY,            // Weapon light, Bows, Throwing
    INTELLIGENCE,       // Magic
    CHARISMA,           // Fellows, Animals, Dialogs
    WISDOM,             // Divine

    MOVEMENT,           // number of fields the character can move

    HEALTH,             // hit points of an character
    ENDURANCE,          // energy to execute endurance based skills
    MANA,               // energy to execute spell based skills

    ATTACK,             // chance to hit
    DEFENCE,            // chance to defend an attack
    ARMOR,              // armor to reduce damage
    DODGE,              // chance to dodge an attack
    CRITICAL,           // chance to perform a critical hit.

    LIMIT_BACKPACK,     // how many items a character can take with him (beside the equipped stuff)
    LIMIT_SPELLS,       // maximum number of spells a character knows.

    MIN_DAMAGE,         // minimum damage
    NAX_DAMAGE,         // maximum damage
    RANGE,              // the range of an action
    RADIUS,             // the radius of an action

}

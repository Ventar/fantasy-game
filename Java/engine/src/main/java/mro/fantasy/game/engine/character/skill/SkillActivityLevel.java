package mro.fantasy.game.engine.character.skill;

import mro.fantasy.game.engine.action.Action;
import mro.fantasy.game.engine.character.equpipment.impl.EquipmentImpl;

/**
 * The activity level of a {@link Skill} indicates if only passive bonuses' area applied to the owner or if the character can perform {@link Action}s based
 * on it . Some passive skills can be used just as a marker to use {@link EquipmentImpl} or to gain access to other skills.
 *
 * @author Michael Rodenbuecher
 * @since 2022-07-26
 */
public enum SkillActivityLevel {

    /**
     * The skill can be used to execute an {@link Action}
     */
    ACTIVE,

    /**
     * The skill is a marker interface
     */
    PASSIVE
}

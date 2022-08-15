package mro.fantasy.game.engine.character.equpipment.impl;

import mro.fantasy.game.engine.gamedata.GameDataSet;
import mro.fantasy.game.engine.character.equpipment.EquipmentType;
import mro.fantasy.game.engine.character.skill.SkillType;

import java.util.List;

public class EquipmentImpl {


    /**
     * The id of this equipment.
     *
     * @see #getId()
     */
    private String id;

    /**
     * The name of the equipment.
     *
     * @see #getName()
     */
    private String name;

    /**
     * The description of the equipment.
     *
     * @see #getDescription()
     */
    private String description;

    private EquipmentType type;

    /**
     * Values of attributes that need to be present to use this equipment.
     */
    private GameDataSet prerequisiteAttributes;

    /**
     * A list of skills that must be present to use this equipment.
     */
    private List<SkillType> prerequisiteSkills;


}

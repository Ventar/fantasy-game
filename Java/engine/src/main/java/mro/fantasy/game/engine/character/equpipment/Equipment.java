package mro.fantasy.game.engine.character.equpipment;

import mro.fantasy.game.engine.character.Character;
import mro.fantasy.game.engine.character.CharacterExtension;
import mro.fantasy.game.engine.character.CharacterExtensionType;

/**
 * Equipment that can be userd by {@link Character} to either get passiv bonuses or to provide executable actions to  them.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-05
 */
public interface Equipment extends CharacterExtension {

    @Override
    default CharacterExtensionType getExtensionType() {
        return CharacterExtensionType.EQUIPMENT;
    }

    /**
     * Returns the type of the equipment. A {@link mro.fantasy.game.engine.character.Character} can only have a certain number of items of a type have equipped, e.g. only one
     * {@link EquipmentType#ARMOR} or two {@link EquipmentType#ONE_HANDED_WEAPON}s.
     */
    EquipmentType getEquipmentType();

    /**
     * Returns information if this is a permanent item or if it can only be used once. Consumable items will be removed from a character after the first usage.
     *
     * @return {@code true} if the item is consumable, {@code false} otherwise
     */
    boolean isConsumable();




}

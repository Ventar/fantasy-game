package mro.fantasy.game.plan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A classification for elements on the plan. The classification is primary used to perform certain algorithms of the game and to distinguish layers and general functionalities of
 * the element.
 *
 * @author Michael Rodenbuecher
 * @since 2022-07-28
 */
public enum FieldType {

    /**
     * Floor elements. The primary use of these elements is to define which fields of the @{@link Plan} are used during a game. In addition, they may offer space for furniture or
     * player characters.
     */
    DUNGEON_FLOOR(1),

    /**
     * A wall on the plan. A wall blocks the sight and movement of players and monsters unless they have special abilities to bypass these limitations.
     */
    DUNGEON_WALL(1),

    /**
     * Additional furniture like shelf's, crates or tables.
     */
    SCENIC(2),

    /**
     * Characters which are managed by players.
     */
    PLAYER(3, true),

    /**
     * Characters which support the players but are managed by the AI.
     */
    FELLOW(3, true),

    /**
     * Characters which are monsters or enemies of the players and fellows.
     */
    ENEMY(3, true);


    /**
     * Logger
     */
    public static final Logger LOG = LoggerFactory.getLogger(FieldType.class);

    /**
     * The value of the z axis. On each layer only one element can be placed on the board, i.e. it is not possible to place a {@link #DUNGEON_WALL} and a {@link #DUNGEON_FLOOR}
     * element on the same board. Adding additional layers to the same field is possible though, i.e. placing a {@link #PLAYER} on a floor
     */
    int layer = 0;

    /**
     * If the layer is a character layer.
     */
    boolean character;

    /**
     * Creates a new plan element on the given layer which is not reserved for characters.
     *
     * @param layer the layer
     */
    FieldType(int layer) {
        this.layer = layer;
    }

    /**
     * Creates a new plan element on the given layer and defines if the element is / holds a character.
     *
     * @param layer     the layer
     * @param character <code>true</code> if it is a character, <code>false</code> otherwise.
     */
    FieldType(int layer, boolean character) {
        this.layer = layer;
        this.character = character;
    }

    /**
     * The layer from bottom to top as integer.
     *
     * @return the layer
     */
    public int getLayer() {
        return layer;
    }

    /**
     * If the layer is reserved for characters.
     *
     * @return <code>true</code> if the layer is for characters, <code>false</code> otherwise
     */
    public boolean isCharacter() {
        return character;
    }

    /**
     * Returns all elements that can contain characters.
     *
     * @return the elements
     */
    public static List<FieldType> getCharacterLayer() {
        ArrayList<FieldType> result = new ArrayList<>();
        for (FieldType e : FieldType.values()) {
            if (e.isCharacter()) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Tries to convert the passed object into a field type. Only Strings which are supported by the {@link #valueOf(String)} function are supported, but this method does not throw
     * an exception but returns <code>null</code> in case the passed parameter cannot be converted.
     *
     * @param o the object to convert
     *
     * @return the field type or <code>null</code> if a conversion is not possible.
     */
    public static FieldType fromObject(Object o) {
        try {
            return valueOf((String) o);
        } catch (Exception e) {
            LOG.warn("Cannot convert ::= [{}] to FieldType:", o, e);
            return null;
        }
    }


}

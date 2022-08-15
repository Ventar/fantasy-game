package mro.fantasy.game.engine.plan;

import java.util.Collection;

/**
 * Container that can hold one or multiple fields and group them together.
 *
 * @author Michael Rodenbuecher
 * @since 2022-07-31
 */
public interface FieldContainer {

    /**
     * Returns all fields of this container in the container coordinate system
     *
     * @return the fields of this container
     */
    Collection<Field> getFields();
}

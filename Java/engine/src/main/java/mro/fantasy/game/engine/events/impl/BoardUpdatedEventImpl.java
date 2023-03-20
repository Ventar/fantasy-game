package mro.fantasy.game.engine.events.impl;

import mro.fantasy.game.Position;
import mro.fantasy.game.devices.board.BoardField;
import mro.fantasy.game.engine.events.BoardUpdatedEvent;

import java.util.List;

/**
 * Implementation of a board update event.
 *
 * @author Michael Rodenbuecher
 * @since 2023-03-11
 */
public record BoardUpdatedEventImpl(List<BoardField> fields) implements BoardUpdatedEvent {

    @Override
    public List<BoardField> getFields() {
        return fields;
    }

    @Override
    public boolean isSensorActive(BoardField.SensorType type, Position position) {
        return fields.stream()                                                             // check all fields of this event
                .filter(f -> f.getPosition().equals(position) && f.isSensorEnabled(type))  // check if position is correct and type is enabled
                .findFirst()                                                               // we will always have a maximum of one result since positions are unique
                .orElse(null) != null;                                               // if we do not find the active sensor return null and convert it to a false
    }
}

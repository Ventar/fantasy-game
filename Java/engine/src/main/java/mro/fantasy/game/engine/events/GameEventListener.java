package mro.fantasy.game.engine.events;

/**
 * Listener that van be registered at a {@link GameEventProducer} to be informed when the producer generates a new event.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-13
 */
public interface GameEventListener<E extends GameEvent> {

    /**
     * Triggered when the {@link GameEventProducer} received an event
     *
     * @param event the event
     */
    void onEvent(E event);

}

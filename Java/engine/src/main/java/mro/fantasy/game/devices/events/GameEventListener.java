package mro.fantasy.game.devices.events;

/**
 * Marker interface for event listener.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-13
 */
public interface GameEventListener<E> {

    /**
     * Marks the listener as one-time listener. A one-time listener is removed from the {@link GameEventProducer} as soon as an event was delivered to it.
     *
     * @return {@code true} if the listener is a one time listener, {@code false} otherwise
     */
    boolean isOneTime();

    /**
     * Triggered when the {@link GameEventProducer} received an event
     * @param event the event
     */
    void onEvent(E event);

}

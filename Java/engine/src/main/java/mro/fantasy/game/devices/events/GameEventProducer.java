package mro.fantasy.game.devices.events;

import java.util.concurrent.Future;

/**
 * Interface to add methods for event listener registration to a class.
 *
 * @param <L> the type of event listener that is used
 * @param <E> the type of event that is handled
 */
public interface GameEventProducer<E extends GameEvent, L extends GameEventListener<E>> extends DeviceEventHandler {

    /**
     * Registers a new event listener which will be triggered when a new event is raised.
     *
     * @param listener the listener to trigger
     */
    void registerListener(L listener);

    /**
     * Removes an existing listener from the sender.
     *
     * @param listener the listener to remove.
     */
    void removeListener(L listener);

    /**
     * Factory method to create the concrete implementation of the event that is sent out to all {@link GameEventListener} which are registered for this provider.
     *
     * @param eventData the device event data
     *
     * @return the event
     */
    E createEvent(DeviceDataPackage eventData);

    /**
     * Returns a future that is resolved when the next event can be provided. This future can be used to block the execution of the current thread until a hardware event of the
     * given type was received and converted into a {@code GameEvent}.
     * <p>
     * Usually that happens when the game engine expects the player to interact with the game devices. Examples are the movement of characters on the board or pressing a certain
     * button on the player controller.
     * <p>
     * To use this functionality it is not necessary to register a listener for this producer. The internal business logic will ensure that the future is automatically resolved as
     * soon as the next event is received.
     *
     * @return the event that was received.
     */
    Future<E> waitForEvent();

}

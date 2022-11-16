package mro.fantasy.game.devices.events.impl;

import mro.fantasy.game.devices.DeviceType;
import mro.fantasy.game.devices.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Abstract implementation of an {@link GameEventProducer}
 *
 * @param <L> the type of event listener that is used
 * @param <E> the type of event that is handled
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-14
 */
public abstract class AbstractGameEventProducer<E extends GameEvent, L extends GameEventListener<E>> implements GameEventProducer<E, L>, DeviceEventHandler {

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(AbstractGameEventProducer.class);

    /**
     * Threadpool to handle device related tasks.
     */
    @Autowired
    private EventThreadPool executor;

    /**
     * Set with registered Listeners
     */
    private Set<L> listenerSet = new HashSet<>();

    /**
     * The type of the device that is handled by this event provider
     */
    private DeviceType deviceType;

    /**
     * A list of futures that wait to be resolved.
     */
    private List<EventCallback> callbacks = new ArrayList<>();

    /**
     * Creates a new instance.
     *
     * @param deviceType the type of the device that is handled by this event provider
     */
    protected AbstractGameEventProducer(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public void registerListener(L listener) {
        LOG.debug("Added event listener ::= [{}] from ::= [{}]", listener, getClass().getSimpleName());
        listenerSet.add(listener);
    }

    @Override
    public void removeListener(L listener) {
        LOG.debug("Removed event listener ::= [{}] from ::= [{}]", listener, getClass().getSimpleName());
        listenerSet.remove(listener);
    }

    /**
     * Factory method to create the concrete implementation of the event that is sent out to all {@link GameEventListener} which are registered for this provider.
     *
     * @param eventData the device event data
     *
     * @return the event
     */
    protected abstract E createEvent(DeviceDataPackage eventData);

    @Override
    public void handle(DeviceDataPackage eventData) {

        // if the device type does not match the event is not handled.
        if (eventData.getDeviceType() != deviceType) {
            LOG.trace("Device event handler ::= [{}] is not interested in event", getClass().getSimpleName());
            return;
        }

        synchronized (this) {                                                               // fine here, we will not have many events
            LOG.debug("Handle event data ::= [{}]", eventData);
            var lastEvent = createEvent(eventData);                                     // inform all listener
            listenerSet.stream().forEach(listener -> listener.onEvent(lastEvent));
            callbacks.forEach(c -> c.setEvent(lastEvent));
            callbacks.clear();
        }

    }

    @Override
    public Future<E> waitForEvent() {
        var callback = new EventCallback();
        callbacks.add(callback);
        return executor.submit(callback);
    }

}

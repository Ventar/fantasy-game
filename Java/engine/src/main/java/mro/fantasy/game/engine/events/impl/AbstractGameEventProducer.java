package mro.fantasy.game.engine.events.impl;

import mro.fantasy.game.engine.events.GameEvent;
import mro.fantasy.game.engine.events.GameEventListener;
import mro.fantasy.game.engine.events.GameEventProducer;
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
public abstract class AbstractGameEventProducer<E extends GameEvent, L extends GameEventListener<E>> implements GameEventProducer<E, L> {

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(AbstractGameEventProducer.class);

    /**
     * Threadpool to handle device related tasks.
     */
    @Autowired
    protected EventThreadPool executor;

    /**
     * Set with registered Listeners
     */
    protected Set<L> listenerSet = new HashSet<>();

    /**
     * A list of futures that wait to be resolved.
     */
    protected List<EventCallback> callbacks = new ArrayList<>();

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

    @Override
    public Future<E> waitForEvent() {
        LOG.debug("Wait for event...");
        var callback = new EventCallback();
        callbacks.add(callback);
        return executor.submit(callback);
    }

    /**
     * Informs all registered listeners about the passed events and resolves the registered callbacks. The difference in the handling is that callbacks are removed / cleared after
     * a one time execution while listeners are permanent until the {@link #removeListener(GameEventListener)} method was called.
     *
     * @param event the event to broadcast
     */
    protected void broadcastEvent(E event) {
        LOG.trace("Broadcast event ::= [{}]", event);
        listenerSet.stream().forEach(listener -> listener.onEvent(event));
        callbacks.forEach(c -> c.setEvent(event));
        //callbacks.forEach(c->c.call());
        callbacks.clear();
    }

}

package mro.fantasy.game.engine.events.impl;


import mro.fantasy.game.engine.events.GameEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Callback that is resolved when an event was received for which another component was waiting.
 *
 * @author Michael Rodenbuehcer
 * @see GameEventProducer#waitForEvent()
 * @since 2022-08-13
 */
public class EventCallback<E> implements Callable<E> {

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(EventCallback.class);


    /**
     * Event to deliver.
     */
    private E event;

    public void setEvent(E event) {
        synchronized (this) {
            LOG.trace("Resolved callback with event ::= [{}]", event);
            this.event = event;
            this.notifyAll();
        }
    }

    @Override
    public E call() throws Exception {

        synchronized (this) {
            while (this.event == null) {
                this.wait();
            }
        }

        return event;
    }
}

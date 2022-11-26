package mro.fantasy.game.engine.events.impl;


import mro.fantasy.game.engine.events.GameEventProducer;

import java.util.concurrent.Callable;

/**
 * Callback that is resolved when an event was received for which another component was waiting.
 *
 * @author Michael Rodenbuehcer
 * @since 2022-08-13
 * @see GameEventProducer#waitForEvent()
 */
public class EventCallback<E> implements Callable<E> {

    /**
     * Event to deliver.
     */
    private E event;

    public void setEvent(E event) {
        synchronized (this) {
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

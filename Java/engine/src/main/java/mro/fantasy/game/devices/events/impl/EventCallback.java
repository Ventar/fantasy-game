package mro.fantasy.game.devices.events.impl;


import mro.fantasy.game.devices.events.GameEvent;

import java.util.concurrent.Callable;

/**
 * Callback that is resolved when an event was set with the {@link #setEvent(GameEvent)}} method.
 *
 * @author Michael Rodenbuehcer
 * @since 2022-08-13
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

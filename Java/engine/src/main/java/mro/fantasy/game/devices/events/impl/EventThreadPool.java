package mro.fantasy.game.devices.events.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Threadpool for the event handling exposed as a Spring component. This will allow the usage of a single threadpool throughout the event handling without the need of creating a
 * new one in every service that needs to support multithreaded code execution.
 *
 * @author Michael Rodenbuecher
 * @see AbstractGameEventProducer
 * @see UDPDeviceEventServiceImpl
 * @since 2022-08-13
 */
@Component
public class EventThreadPool extends ThreadPoolExecutor {

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(EventThreadPool.class);

    /**
     * Creates a new threadpool with default parameters for the application.
     */
    public EventThreadPool() {
        super(2, 8, 1,
                TimeUnit.HOURS,
                new ArrayBlockingQueue<>(1000),
                new ThreadFactory() {
                    private final AtomicInteger number = new AtomicInteger(0);

                    @Override
                    public Thread newThread(@NonNull Runnable r) {
                        Thread t = new Thread(r);
                        t.setName("EVENT-" + (number.incrementAndGet()));
                        return t;
                    }
                },
                (runnable, executor) -> LOG.warn("Thread execution for event handling was rejected")         // RejectExecutionListener
        );
    }
}

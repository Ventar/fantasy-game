package mro.fantasy.game.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An endless thread that catches exceptions and print them into to the underlying logger.
 *
 * @author Michael Rodenbucher
 * @since 2022-11-23
 */
public abstract class ServiceThread extends Thread {

    /**
     * Logger.
     */
    public static final Logger LOG = LoggerFactory.getLogger(ServiceThread.class);


    /**
     * The logger used by this thread instead of the default {@link #LOG} logger
     */
    private Logger logger;

    /**
     * The thread sleep time before every execution of the {@link #work()} method
     */
    private long sleep = 0;

    /**
     * Sets the logger used by this thread instead of the default {@link #LOG} logger
     *
     * @param logger the logger to use
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Sets the thread sleep time before every execution of the {@link #work()} method
     *
     * @param sleep the sleep time in ms
     */
    public void setSleep(long sleep) {
        this.sleep = sleep;
    }

    /**
     * Code executed in the {@link #run()} method of this thread.
     *
     * @throws Exception in case of an error
     */
    public abstract void work() throws Exception;

    /**
     * Opens a UDP socket connection and listen for incoming datagram packets from devices which are connected to the game server.
     */
    @Override
    public void run() {
        while (true) {
            try {

                if (sleep > 0) {
                    Thread.sleep(sleep);
                }

                work();

            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted", e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                if (logger != null) {
                    logger.warn("\n\n !!! Error during thread execution !!! \n\n", e);
                } else {
                    LOG.warn("\n\n !!! Error during thread execution !!! \n\n", e);
                }

            }
        }
    }

}

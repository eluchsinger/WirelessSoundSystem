package utils.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 25.03.2016.
 * Provides some useful utilities for handling ExecutorService objects.
 */
public class ExecutorServiceUtils {
    /**
     * The default time to shutdown an executor is 5 seconds.
     */
    public final static long DEFAULT_SHUTDOWN_TIMEOUT = 5 * 1000;

    /**
     * Stops the executor service.
     * First tries to stop the executor in a soft way, then after timeout it will
     * force the executor to stop.
     * For more information look up Executors at:
     * http://winterbe.com/posts/2015/04/07/java8-concurrency-tutorial-thread-executor-examples/
     *
     * This method does shuts down using the default timeout (DEFAULT_SHUTDOWN_TIMEOUT).
     *
     * @param executorService The executor service to stop.
     */
    public static void stopExecutorService(ExecutorService executorService) {
        ExecutorServiceUtils.stopExecutorService(executorService, DEFAULT_SHUTDOWN_TIMEOUT);
    }

    /**
     * Stops the executor service.
     * First tries to stop the executor in a soft way, then after timeout it will
     * force the executor to stop.
     * For more information look up Executors at:
     * http://winterbe.com/posts/2015/04/07/java8-concurrency-tutorial-thread-executor-examples/
     * @param executorService The executor service to stop.
     * @param timeout The timeout time in milliseconds.
     */
    public static void stopExecutorService(ExecutorService executorService, long timeout) {
        Logger logger = Logger.getLogger(ExecutorServiceUtils.class.getName());

        if(executorService != null && !executorService.isShutdown()) {
            try {
                logger.log(Level.INFO, "Attempt to shutdown executor...");
                executorService.shutdown();
                if (!executorService.isShutdown())
                    executorService.awaitTermination(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Stop executor service interrupted", e);
            } finally {
                if (!executorService.isShutdown()) {
                    logger.log(Level.SEVERE, "Cancelling non-interrupted executor");
                }
                executorService.shutdownNow();
                logger.log(Level.INFO, "Executor shutdown finished");
            }
        }
        // If the executor is already null or shutdown.
        else {
            if(executorService == null)
                logger.log(Level.WARNING, "Tried to shut down null executor");
            if(executorService.isShutdown())
                logger.log(Level.INFO, "Executor service is alredy shutdown.");
        }

    }
}

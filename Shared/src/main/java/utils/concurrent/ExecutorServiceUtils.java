package utils.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 25.03.2016.
 */
public class ExecutorServiceUtils {

    /**
     * Stops the executor service.
     * First tries to stop the executor in a soft way, then after timeout it will
     * force the executor to stop.
     * For more information look up Executors at:
     * http://winterbe.com/posts/2015/04/07/java8-concurrency-tutorial-thread-executor-examples/
     * @param executorService The executor service to stop.
     */
    public static void stopExecutorService(ExecutorService executorService) {
        Logger logger = Logger.getLogger(ExecutorServiceUtils.class.getName());
        try {
            logger.log(Level.INFO, "Attempt to shutdown executor in TCP Streaming Service.");
            executorService.shutdown();
            if(!executorService.isShutdown())
                executorService.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch(InterruptedException e) {
            logger.log(Level.WARNING, "Stop executor service interrupted", e);
        }
        finally {
            if(!executorService.isShutdown()) {
                logger.log(Level.SEVERE, "Cancelling non-finished executor");
            }
            executorService.shutdownNow();
            logger.log(Level.INFO, "shutdown finished");
        }
    }
}

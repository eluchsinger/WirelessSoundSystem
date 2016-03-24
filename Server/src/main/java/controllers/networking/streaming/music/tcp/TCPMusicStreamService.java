package controllers.networking.streaming.music.tcp;

import controllers.networking.streaming.music.tcp.callbacks.OnObjectReceived;
import models.NetworkClient;
import models.SocketChannelNetworkClient;
import models.clients.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 23.03.2016.
 */
public class TCPMusicStreamService {
    /**
     * Amount of time that the application will wait for the executor
     * to stop working, when the service is stopped.
     * If the time is exceeded, the Executor will be forced to stop.
     */
    public final static int EXECUTOR_CLOSING_AWAITING_TIME = 1000;

    private ServerSocketChannel serverSocketChannel;
    /**
     * The acceptSelector is looking for new clients connecting.
     */
    private final Selector acceptSelector;
    /**
     * The readSelector is looking for new inbound reads.
     */
    private final Selector readSelector;

    /**
     * The readSelectorLock is used in order to synchronize the readSelector, because
     * it is registered in the acceptance thread and the select() method is called
     * on a different thread.
     * The register() method can't be called while the Selector is using the select() (blocking-call).
     */
    private Lock readSelectorLock;

    /**
     * The executorService of the acceptance method.
     * Handles new connections from clients.
     */
    private ExecutorService acceptanceExecutorService;

    /**
     * The reading executorService.
     * Handles incoming messages from the clients.
     */
    private ExecutorService readingExecutorService;

    /**
     * The isRunning boolean is there to cancel running threads.
     */
    private volatile boolean isRunning  = false;

    private final List<OnObjectReceived> onObjectReceivedListeners;
    private final List<NetworkClient> connectedClients;

    /**
     * Initializes the variables of this class, including the
     * ServerSocket.
     * (The listening is not yet running, call start() to run.)
     * @throws IOException
     */
    public TCPMusicStreamService() throws IOException {

        this.onObjectReceivedListeners = new ArrayList<>();
        this.connectedClients = new ArrayList<>();

        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.configureBlocking(false);
        this.serverSocketChannel.bind(new InetSocketAddress(Server.STREAMING_PORT));
        this.acceptSelector = Selector.open(); // Register to the server
        this.readSelector = Selector.open(); // Register to the clients.
        this.serverSocketChannel.register(this.acceptSelector,
                SelectionKey.OP_ACCEPT);

        this.isRunning = true;

        // Init the readselectorLock
        this.readSelectorLock = new ReentrantLock();

        this.acceptanceExecutorService = Executors.newSingleThreadExecutor();
        this.readingExecutorService = Executors.newSingleThreadExecutor();
    }
    /**
     * Starts the streaming service.
     * @throws IOException
     */
    public void start() throws IOException {
        this.acceptanceExecutorService.submit(this::acceptanceMethod);
        this.readingExecutorService.submit(this::readTask);
    }

    /**
     * Stops the streaming service and frees the resources.
     * @throws IOException
     */
    public void stop() throws IOException {
        this.isRunning = false;
        this.stopExecutorService(this.acceptanceExecutorService);
        this.stopExecutorService(this.readingExecutorService);
        this.serverSocketChannel.close();
    }

    /**
     * Stops the executor service.
     * First tries to stop the executor in a soft way, then after timeout it will
     * force the executor to stop.
     * For more informations look up Executors at:
     * http://winterbe.com/posts/2015/04/07/java8-concurrency-tutorial-thread-executor-examples/
     * @param executorService The executor service to stop.
     */
    private void stopExecutorService(ExecutorService executorService) {
        Logger logger = Logger.getLogger(this.getClass().getName());
        try {
            logger.log(Level.INFO, "Attempt to shutdown executor in TCP Streaming Service.");
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch(InterruptedException e) {
            logger.log(Level.INFO, "Task interrupted", e);
        }
        finally {
            if(!executorService.isShutdown()) {
                logger.log(Level.SEVERE, "Cancelling non-finished executor");
            }
            executorService.shutdownNow();
            logger.log(Level.INFO, "shutdown finished");
        }
    }

    //region Acceptance Multi-Thread
    /**
     * This method accepts the new clients with inbound connections.
     */
    private void acceptanceMethod() {
        while(this.isRunning) {
            synchronized (this.acceptSelector) {
                try {
                    this.acceptSelector.select(); // Blocking call, wakes up when there is something to accept.
                    Set<SelectionKey> readyKeys = this.acceptSelector.selectedKeys();
                    Iterator<SelectionKey> iterator = readyKeys.iterator();

                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isAcceptable()) {
                            // Accept new client.
                            SocketChannel client = this.serverSocketChannel.accept();
                            System.out.println("Connected Client: " + client);
                            client.configureBlocking(false);

                            // Todo: Add new client to client-list.
                            this.registerReadSelector(client);

                            this.connectedClients.add(new SocketChannelNetworkClient(client));
                        }
                    }
                }
                catch(IOException ioException) {
                    Logger.getLogger(this.getClass().getName())
                            .log(Level.SEVERE, "Error in the acceptance method", ioException);
                }
                catch(InterruptedException interruptedException) {
                    Logger.getLogger(this.getClass().getName())
                            .log(Level.SEVERE, "ReadSelectorLock interrupted while waiting for lock!",
                                    interruptedException);
                }
            }
        }
    }

    /**
     * Try to register the readSelector for the client.
     * @param client The client that should be registered on the readselector.
     * @throws InterruptedException Throws an interrupted exception, if the thread on which this method is running was interrupted.
     * @throws IOException If the operation failed, there will be an IOException thrown.
     */
    private void registerReadSelector(SocketChannel client) throws InterruptedException, IOException {

        int retries = 0;
        int maxRetries = 1;

        while(retries < maxRetries) {
            if(this.readSelectorLock.tryLock(500L, TimeUnit.MILLISECONDS)){
                try {
                    System.out.println("Client readSelector registered...");
                    client.register(this.readSelector, SelectionKey.OP_READ);
                }
                finally {
                    this.readSelectorLock.unlock();
                }
            } else if(retries < maxRetries){
                retries++;
                // There are still retries left.
                this.readSelector.wakeup();
            } else {
                // No more retries
                throw new IOException("Failed registering the readSelector" +
                        "on the new client.");
            }
        }
    }
    //endregion Acceptance Multi-Thread

    private void readTask() {
        while(this.isRunning) {
            try {
                this.readSelector.select();

                Set<SelectionKey> readKeys = this.readSelector.selectedKeys();
                Iterator<SelectionKey> iterator = readKeys.iterator();

                while(iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if(key.isReadable()) {
                        // Todo: Maybe returns a wrong channel: Check it!
                        SocketChannel socketChannel = (SocketChannel)key.channel();

                        // Todo: List synchronization before using it!
                        Optional<NetworkClient> clientOptional = this.connectedClients.stream()
                                .filter(nc -> nc.getSocket().getChannel().equals(socketChannel))
                                .findFirst();

                        if(clientOptional.isPresent()) {
                            try {
                                Object obj = clientOptional.get().getObjectInputStream().readObject();

                                if(obj != null) {
                                    this.onObjectReceived(obj);
                                }
                            } catch (ClassNotFoundException e) {
                                Logger.getLogger(this.getClass().getName())
                                        .log(Level.WARNING, "Class not found reading object", e);
                            }
                        }
                    }
                }

            } catch (IOException ioException) {
                Logger.getLogger(this.getClass().getName())
                        .log(Level.SEVERE, "Error in reading method", ioException);
            }
        }
    }

    //region Listeners

    /**
     * Adds a listener, in case an object is received.
     * If there are multiple listeners added, all the listeners will be called.
     * @param listener Listener to add.
     */
    public void addOnObjectReceivedListener(OnObjectReceived listener){
        this.onObjectReceivedListeners.add(listener);
    }

    /**
     * Removes the listener. If the listener was added multiple times, the listener has to
     * be removed an equal amount of times.
     * @param listener Listener to remove.
     * @return Returns true, if the listener was removed, false if no listener was found.
     */
    public boolean removeOnObjectReceivedListener(OnObjectReceived listener) {
        return this.onObjectReceivedListeners.remove(listener);
    }

    /**
     * Fires the object received callbacks.
     * Thread-Safe.
     * @param object Object received
     */
    private void onObjectReceived(Object object) {
        synchronized(this.onObjectReceivedListeners) {
            this.onObjectReceivedListeners.forEach(onObjectReceived -> onObjectReceived.onObjectReceived(object));
        }
    }
    //endregion Listeners
}

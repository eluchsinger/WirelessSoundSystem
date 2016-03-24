package controllers.networking.streaming.music.tcp;

import controllers.io.cache.file.FileCacheService;
import controllers.io.cache.file.StaticFileCacheService;
import models.clients.Server;
import models.networking.clients.NetworkClient;
import models.networking.clients.SocketChannelNetworkClient;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

/**
 * Created by Esteban Luchsinger on 24.03.2016.
 * TCP Music Streaming Service using the Java NIO SocketChannels.
 */
public class TCPMusicStreamingService implements Closeable {
    /**
     * Set the channel mode to blocking (true) or non-blocking (false).
     */
    private static final boolean BLOCKING_MODE = true;
    private NetworkClient networkClient;
    private final Selector readingSelector;

    private Server currentServer;

    /**
     * File cache. The songs have to be cached here when they
     * were received completely.
     */
    private final FileCacheService cache;

    public TCPMusicStreamingService() throws IOException {

        this.readingSelector = Selector.open();
        this.cache = new StaticFileCacheService();
    }

    /**
     * @return Returns the current server.
     */
    public Server getCurrentServer() {
        return currentServer;
    }

    /**
     * Sets the current server.
     * @param server the new current server.
     */
    public void setCurrentServer(Server server) throws IOException {

        // Just do stuff, if the server really changed.
        if(this.currentServer != server) {
            this.currentServer = server;

            if(this.currentServer != null) {
                if(this.networkClient != null) {
                    this.networkClient.close();
                }
                SocketChannel channel = SocketChannel.open();
                channel.configureBlocking(true);
                channel.connect(new InetSocketAddress(this.getCurrentServer().getServerAddress(),
                                Server.STREAMING_PORT));

                while(!channel.finishConnect()) {
                    try {
                        System.out.println("Waiting for finishConnect()");
                        TimeUnit.MILLISECONDS.sleep(1000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                synchronized (this.readingSelector) {

                }

                this.networkClient = new SocketChannelNetworkClient(channel);
            }
        }
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     * <p>
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        if(this.networkClient != null)
            this.networkClient.close();
    }
}
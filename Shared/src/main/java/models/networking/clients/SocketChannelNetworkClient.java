package models.networking.clients;

import models.clients.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * Created by Esteban Luchsinger on 24.03.2016.
 * A NetworkClient implementation using the NIO SocketChannel.
 */
public class SocketChannelNetworkClient extends Client implements NetworkClient {

    private final SocketChannel socketChannel;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;

    /**
     * Constructor
     * @param socketChannel Open socket channel.
     * @throws IOException
     */
    public SocketChannelNetworkClient(SocketChannel socketChannel) throws IOException {
        this.socketChannel = socketChannel;
        this.objectOutputStream = new ObjectOutputStream(this.socketChannel.socket().getOutputStream());
        this.objectInputStream = new ObjectInputStream(this.socketChannel.socket().getInputStream());
    }

    /**
     * @return Returns the ObjectOutputStream opened for the SocketChannel.
     */
    @Override
    public ObjectOutputStream getObjectOutputStream() {
        return this.objectOutputStream;
    }

    /**
     * @return Returns the ObjectInputStream opened for the SocketChannel
     */
    @Override
    public ObjectInputStream getObjectInputStream() {
        return this.objectInputStream;
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
        this.socketChannel.close();
        this.objectOutputStream.close();
        this.objectInputStream.close();
    }

    /**
     * @return Returns the current socket.
     */
    public Socket getSocket(){
        return this.socketChannel.socket();
    }
}

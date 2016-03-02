package models.clients;

import java.net.InetAddress;

/**
 * Created by Esteban Luchsinger on 08.12.2015.
 */
public class Server {
    private InetAddress serverAddress;
    private int serverListeningPort;

    public Server(InetAddress serverAddress, int serverListeningPort){
        this.serverAddress = serverAddress;
        this.serverListeningPort = serverListeningPort;
    }

    public InetAddress getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(InetAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getServerListeningPort() {
        return serverListeningPort;
    }

    public void setServerListeningPort(int serverListeningPort) {
        this.serverListeningPort = serverListeningPort;
    }

    @Override
    public String toString() {
        return this.serverAddress.getHostAddress() + " (Port: " + this.serverListeningPort + ")";
    }
}

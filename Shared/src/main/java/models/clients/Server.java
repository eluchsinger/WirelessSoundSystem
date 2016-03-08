package models.clients;

import java.net.InetAddress;
import java.util.Objects;

/**
 * Created by Esteban Luchsinger on 08.12.2015.
 * This class defines a server with an IP-Address and a listening port.
 */
public class Server {
    /**
     * Default listening port for the server (only streaming).
     * If you want to connect to the server to receive music over it's TCP-Service, connect to this port.
     */
    public final static int STREAMING_PORT = 6070;

    /**
     * Default server listening port for the discovery service.
     * Send ACKS to this port!
     */
    public final static int DISCOVERY_PORT = 6584;

    // Address of the server.
    private InetAddress serverAddress;
    // Port listening for clients.
    private int serverListeningPort;

    /**
     * Constructor for this object.
     * @param serverAddress Server Address
     * @param serverListeningPort Listening Port
     */
    public Server(InetAddress serverAddress, int serverListeningPort){
        this.serverAddress = serverAddress;
        this.serverListeningPort = serverListeningPort;
    }

    /**
     * Gets the server address.
     * @return Server InetAddress
     */
    public InetAddress getServerAddress() {
        return serverAddress;
    }

    /**
     * Sets the server address.
     * @param serverAddress new server InetAddress
     */
    public void setServerAddress(InetAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * Gets the server listening port.
     * @return Server listening port.
     */
    public int getServerListeningPort() {
        return serverListeningPort;
    }

    /**
     * Sets the server listening port.
     * @param serverListeningPort new listening port value.
     */
    public void setServerListeningPort(int serverListeningPort) {
        this.serverListeningPort = serverListeningPort;
    }

    /**
     * Compares the current server with an other object.
     * A server is equals, if:
     *  - The object being compared IS the same object (same reference).
     *  - Both compared servers have the same ServerAddress AND the same Port.
     * @param obj Object to compare with.
     * @return Returns true, if the compared objects are equal.
     */
    @Override
    public boolean equals(Object obj) {

        if(obj == null)
            return false;
        if(obj == this)
            return true;

        if(obj.getClass().equals(getClass())) {
            Server other = (Server)obj;

            return this.getServerAddress().getHostAddress().equals(other.getServerAddress().getHostAddress())
                    && this.getServerListeningPort() == other.getServerListeningPort();
        }
        else {
            return false;
        }
    }

    /**
     * Creates the hashCode of this Server Object.
     * The hashcode are made of the server address and the port.
     * @return Hashcode.
     */
    @Override
    public int hashCode() {
        return Objects.hash(serverAddress, serverListeningPort);
    }

    /**
     * Creates a string with representing the server Object.
     * @return String: "<HostAddress> (Port: <Port>)"
     */
    @Override
    public String toString() {
        return this.serverAddress.getHostAddress() + " (Port: " + this.serverListeningPort + ")";
    }
}

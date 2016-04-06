package models.clients;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.net.InetAddress;
import java.time.LocalDateTime;

/**
 * Created by Esteban Luchsinger on 30.11.2015.
 * This class describes the clients.
 */
public class Client implements Comparable {
    //region Members
    private SimpleStringProperty name;
    private SimpleObjectProperty<InetAddress> inetAddress;
    private LocalDateTime lastSeen;
    //endregion

    //region Constructors

    /**
     * Default Constructor
     */
    public Client(){
        this(InetAddress.getLoopbackAddress(), "");
    }

    /**
     * Constructor with parameters
     * @param inetAddress
     * @param name
     */
    public Client(InetAddress inetAddress, String name){
        this.name = new SimpleStringProperty(name);
        this.inetAddress = new SimpleObjectProperty<>(inetAddress);
        this.lastSeen = LocalDateTime.now();
    }

    //endregion

    //region Accessors

    public InetAddress getInetAddress() {
        return inetAddress.get();
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress.set(inetAddress);
    }

    public String getName() {
        return name.getValueSafe();
    }

    public void setName(String name) {
        this.name.setValue(name);
    }
    //endregion

    //region Properties
    public SimpleObjectProperty<InetAddress> inetAddressProperty() {
        return inetAddress;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }
    //endregion

    //region Methods

    /**
     * Returns a string composed of the name and the HostAddress.
     * Example: Living Room [192.168.1.1]
     * @return
     */
    @Override
    public String toString(){
        return this.getName() + " [" + this.getInetAddress().getHostAddress() + "]";
    }

    @Override
    public int compareTo(Object o) {

        if(o.getClass().equals(this.getClass())) {
            if(this.getInetAddress().equals(((Client)o).getInetAddress())){
                return 0;
            }
            else{
                // Compare the length of both InetAddresses.
                if(((Client) o).getInetAddress().getAddress().length > this.getInetAddress().getAddress().length)
                    return 1;
                else
                    return -1;
            }
        }
        else{
            throw new IllegalArgumentException("The compared types are not compatible");
        }
    }

    /**
     * Compares two Clients for equality.
     * Two clients are equal if the InetAddress is the same.
     * If there is no InetAddress, the objects equality can not be determined and false is returned.
     * @param obj Other object to comapare
     * @return Returns true if the compared objects are equal. False if they are not.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass() == this.getClass()) {
            Client c = (Client) obj;

            // Try to compare the InetAddress.
            if (c.getInetAddress() != null && c.getInetAddress().equals(this.getInetAddress())) {
                return true;
            } else return false;
        } else {
            return false;
        }
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    //endregion
}

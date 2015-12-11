package ch.wirelesssoundsystem.shared.models.clients;

import com.sun.corba.se.impl.io.TypeMismatchException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.net.InetAddress;

/**
 * Created by Esteban Luchsinger on 30.11.2015.
 * This class describes the clients.
 */
public class Client implements Comparable {
    //region Members
    private SimpleStringProperty name;
    private SimpleObjectProperty<InetAddress> inetAddress;
    //endregion

    //region Constructors
    public Client(InetAddress inetAddress, String name){
        this.name = new SimpleStringProperty(name);
        this.inetAddress = new SimpleObjectProperty<>(inetAddress);
    }

    public Client(InetAddress inetAddress){
        this(inetAddress, "");
    }

    @Deprecated
    public Client(String name){
        this.name = new SimpleStringProperty(name);
        this.inetAddress = new SimpleObjectProperty<>();
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
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
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
    @Override
    public String toString(){
        return this.getName();
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
            throw new TypeMismatchException("The compared types are not compatible");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(this.getInetAddress() != null && obj.getClass() == this.getClass()){
            Client c = (Client)obj;

            if(c.getInetAddress() != null && c.getInetAddress().equals(this.getInetAddress())){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }

    //endregion
}

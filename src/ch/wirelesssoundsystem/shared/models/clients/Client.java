package ch.wirelesssoundsystem.shared.models.clients;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.net.InetAddress;

/**
 * Created by Esteban Luchsinger on 30.11.2015.
 * This class describes the clients.
 */
public class Client {
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
    //endregion
}

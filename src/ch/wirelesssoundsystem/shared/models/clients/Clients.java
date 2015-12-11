package ch.wirelesssoundsystem.shared.models.clients;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Created by Esteban Luchsinger on 11.12.2015.
 */
public class Clients {
    private ObservableList<Client> clients;
    private static Clients ourInstance = new Clients();

    public static Clients getInstance() {
        return ourInstance;
    }

    private Clients() {
        this.clients = FXCollections.observableArrayList();
    }

    public ObservableList<Client> getClients(){
        return this.clients;
    }
}

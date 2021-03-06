package models.clients;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 11.12.2015.
 * A list of clients.
 * (ObservableList)
 */
public class Clients {
    private ObservableList<Client> clients;
    private static Clients ourInstance = new Clients();

    /**
     * Gets the instance of the singleton.
     * @return The instance.
     */
    public static Clients getInstance() {
        return ourInstance;
    }


    private Clients() {
        this.clients = FXCollections
                .synchronizedObservableList(FXCollections.observableArrayList());
    }

    /**
     * Gets the observable-List of the clients.
     * @return Observable List.
     */
    public ObservableList<Client> getClients(){
        return this.clients;
    }

    /**
     * Call this method, if a client is seen on the network.
     * This method adds the client to the list, if needed and updates its lastSeen value.
     * @param client Client that was seen.
     */
    public void seenClient(Client client){

        Client clientInList;

        // If client is not in the list of clients.
        if(!this.getClients().contains(client)){
            this.getClients().add(client);
            clientInList = client;
            System.out.println("Client added: "  + client.getInetAddress().getHostAddress());
        }
        else {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Client already exists in the Clients list...");

            // Search for the client
            Optional<Client> maybeClient = this.getClients().stream()
                    .filter(c -> c.equals(client))
                    .findFirst();

            clientInList = maybeClient.orElse(null);
        }

        if(clientInList != null){
            clientInList.setLastSeen(LocalDateTime.now());
        }
        else {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "seenClient error", new NullPointerException("Client was not found in the list and could not be added."));
        }
    }
}

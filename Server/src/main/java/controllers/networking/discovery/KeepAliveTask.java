package controllers.networking.discovery;

import models.clients.Client;
import javafx.concurrent.Task;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Esteban Luchsinger on 11.12.2015.
 */
public class KeepAliveTask extends Task<List<Client>> {

    private final List<Client> clients;
    private final long timeout;
    private final LocalDateTime currentDateTime;

    /**
     * Initialize the KeepAlive Task.
     * @param originalList Original List of clients to check.
     * @param timeout Timeout parameter in milliseconds.
     */
    public KeepAliveTask(List<Client> originalList, long timeout, LocalDateTime currentDateTime){
        this.clients = originalList;
        this.timeout = timeout;
        this.currentDateTime = currentDateTime;
    }

    /**
     * This is the method which checks the last seen field of the clients.
     * @return A list with expired clients.
     * @throws Exception
     */
    @Override
    protected List<Client> call() {
        List<Client> expiredClients = new ArrayList<>();

        for(Client client : this.clients){
            long difference = client.getLastSeen().until(currentDateTime, ChronoUnit.MILLIS);

            if(difference > timeout){
                expiredClients.add(client);
            }
        }

        return expiredClients;
    }
}

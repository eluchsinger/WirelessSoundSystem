package controllers.networking.discovery.callbacks;

import models.clients.Client;

/**
 * Created by Esteban on 18.03.2016.
 * This interface is called when a client was found.
 */
@FunctionalInterface
public interface OnClientFound {
    void onFoundClient(Client client);
}

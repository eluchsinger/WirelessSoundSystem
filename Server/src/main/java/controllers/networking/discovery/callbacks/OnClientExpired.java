package controllers.networking.discovery.callbacks;

import models.clients.Client;

/**
 * Created by Esteban on 18.03.2016.
 * This interface is called when a client expired.
 */
@FunctionalInterface
public interface OnClientExpired {
    void onClientExpired(Client client);
}

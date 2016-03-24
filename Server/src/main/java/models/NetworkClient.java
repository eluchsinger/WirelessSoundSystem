package models;

import java.io.Closeable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Esteban Luchsinger on 24.03.2016.
 */
public interface NetworkClient extends Closeable {

    ObjectOutputStream getObjectOutputStream();
    ObjectInputStream getObjectInputStream();
    Socket getSocket();


}

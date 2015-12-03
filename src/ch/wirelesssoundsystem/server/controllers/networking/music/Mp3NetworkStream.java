package ch.wirelesssoundsystem.server.controllers.networking.music;

import ch.wirelesssoundsystem.server.controllers.networking.NetworkStream;
import ch.wirelesssoundsystem.server.controllers.networking.Utility;
import ch.wirelesssoundsystem.server.models.songs.Song;
import sun.net.util.IPAddressUtil;

import java.io.IOException;
import java.net.*;

/**
 * Created by Esteban Luchsinger on 03.12.2015.
 */
public class Mp3NetworkStream implements NetworkStream<Song> {
    private final int clientSocketPort = 55654;
    private Socket streamSocket;

    public Mp3NetworkStream() throws IOException {
        this.streamSocket = new Socket(Utility.getBroadcastAddress4(), clientSocketPort);
    }

    @Override
    public void startStream(Song data) {

    }

    @Override
    public void stopStream() {

    }
}

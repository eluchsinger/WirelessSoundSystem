package controllers.networking.streaming.music.tcp;

import controllers.clients.ClientController;
import controllers.networking.streaming.music.MusicStreamController;
import models.networking.clients.NetworkClient;
import models.networking.dtos.CacheSongCommand;
import models.networking.dtos.PauseCommand;
import models.networking.dtos.PlayCommand;
import models.networking.dtos.StopCommand;
import models.songs.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.media.SongUtils;

import java.io.IOException;


/**
 * Created by Esteban on 29.03.2016.
 * This class controls the Music Stream.
 * It does not directly handle any connection issues, but uses the I/O Streams provided by the Socket of the clients.
 * The clients are provided by the ClientController.
 */
public class TCPMusicStreamController implements MusicStreamController {

    /**
     * The logger of this class.
     * Just for comfort.
     */
    private final Logger logger;

    /**
     * The client controller bound to this MusicController.
     */
    private final ClientController clientController;

    /**
     * Creates a new instance of a music stream controller.
     * @param clientController The client controller bound to this music controller.
     */
    public TCPMusicStreamController(ClientController clientController) {
        this.logger = LoggerFactory.getLogger(this.getClass());

        this.clientController = clientController;
    }

    /**
     * Starts playing the song on the client.
     *
     * @param song Song to stream.
     * @throws IOException Throws an IO Exception if there was a problem with IO. (Probably with the song).
     */
    @Override
    public void play(Song song) throws IOException {
        byte[] songData = SongUtils.getSongData(song);

        // Initialize this here, so we have less delay between sending of the objects.
        PlayCommand playCommand = new PlayCommand(song.getTitle(), song.getArtist());

        this.cacheSong(songData);

        // Then wait for the clients until they all received the song completely.
        this.waitForClientsReceived();

        // Then send all clients the play command.
        for(NetworkClient client : this.clientController.getClients()) {
            client.send(playCommand);
        }
    }

    /**
     * Stops playing the song on the client.
     */
    @Override
    public void stop() {
        StopCommand stopCommand = new StopCommand();
        for(NetworkClient client : this.clientController.getClients()) {
            client.send(stopCommand);
        }
    }

    /**
     * Pauses the song on the client.
     */
    @Override
    public void pause() {
        PauseCommand pauseCommand = new PauseCommand();
        for(NetworkClient client : this.clientController.getClients()) {
            client.send(pauseCommand);
        }
    }

    /**
     * Caches a song on the clients.
     *
     * @param song The song to cache on the clients.
     */
    @Override
    public void cacheSong(Song song) throws IOException {
        byte[] songData = SongUtils.getSongData(song);
        this.cacheSong(songData);
    }

    /**
     * Caches a song on the clients.
     *
     * @param songData The bytes containing the song data.
     */
    private void cacheSong(byte[] songData) {
        CacheSongCommand command = new CacheSongCommand(songData);

        // Send the cache song command to make the clients ready.
        for(NetworkClient client : this.clientController.getClients()) {
            client.send(command);
        }
    }

    /**
     * Waits, until all clients have received pending messages.
     * Used to synchronize asynchronous sending of objects.
     */
    private void waitForClientsReceived() {

        // Wait for all clients to receive the messages.
        this.clientController.getClients().forEach(NetworkClient::waitForSending);
    }
}
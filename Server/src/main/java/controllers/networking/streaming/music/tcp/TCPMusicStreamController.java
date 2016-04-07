package controllers.networking.streaming.music.tcp;

import controllers.clients.ClientController;
import controllers.networking.streaming.music.MusicStreamController;
import models.networking.clients.NetworkClient;
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

        PlayCommand playCommand = new PlayCommand(song.getTitle(), song.getArtist(), songData);

        for(NetworkClient client : this.clientController.getClients()) {
            try {
                client.getObjectOutputStream().writeObject(playCommand);
            }
            catch(IOException ioException) {
                this.logger.warn("Error sending a play command to the client: " + client, ioException);
            }
        }
    }

    /**
     * Stops playing the song on the client.
     */
    @Override
    public void stop() {
        StopCommand stopCommand = new StopCommand();
        for(NetworkClient client : this.clientController.getClients()) {
            try {
                client.getObjectOutputStream().writeObject(stopCommand);
            }
            catch(IOException ioException) {
                this.logger.warn("Error sending a stop command to the client: " + client, ioException);
            }
        }
    }
}
package controllers.networking.streaming.music.udp;

import controllers.networking.streaming.music.MusicStreamController;
import models.clients.Clients;
import models.networking.SongCache;
import models.networking.SongDatagram;
import models.networking.exceptions.StreamingInitFailedException;
import models.networking.messages.StreamingMessage;
import models.songs.Song;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 07.03.2016.
 * Streams Music using the UDP Protocol.
 */
public class UDPMusicStreamController implements MusicStreamController {

    /**
     * The destination port for datagrams sent to the clients.
     */
    private final static int CLIENT_PORT = 6049;

    /**
     * Multicast address group.
     */
    private final static String MULTICAST_GROUP_ADDRESS = "239.255.42.100";

    /**
     * Timeout for the streaming responses in milliseconds.
     * (i.e.: The stream init response)
     */
    private final static int TIMEOUT_FOR_RESPONSES = 1000;

    /**
     * Amount of retries allowed for the initialization process, before throwing an exception.
     */
    private final static int STREAM_INITIALIZATION_RETRIES = 1;

    /**
     * The song cache containing the different song segments.
     */
    private SongCache songCache;

    /**
     * Starts the streaming of a song.
     * @param song Song to be streamed.
     */
    public void play(Song song) {
        try {
            this.songCache = this.cacheSong(song);
            this.reportStartStreaming(this.songCache);
            this.streamSong();
            this.reportFinishedStreaming();

        } catch (IOException | StreamingInitFailedException e) {
            Logger.getLogger(this.getClass().getName())
                    .log(Level.SEVERE, "Error playing " + song.getTitle() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void stopPlaying() {
        throw new RuntimeException("Not Implemented method");
    }

    /**
     * Caches the song.
     * @param song Song to cache
     * @return Returns a SongCache containing the desired song.
     * @throws IOException If there was an error.
     */
    private SongCache cacheSong(Song song) throws IOException {
        return SongCache.cacheSong(song,
                InetAddress.getByName(MULTICAST_GROUP_ADDRESS),
                CLIENT_PORT);
    }

    /**
     * Initializes the streaming.
     * Sends a packet containing the stream structure (i.e.: amount of packets going to be streamed.)
     */
    private void reportStartStreaming(SongCache cache) throws IOException, StreamingInitFailedException {

        String initMessage = StreamingMessage.initializationMessage(cache.getExpectedCacheSize());
        String ackMessage = StreamingMessage.initializationAckMessage(cache.getExpectedCacheSize());
        byte[] data = (initMessage).getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(data,
                0,
                data.length,
                InetAddress.getByName(MULTICAST_GROUP_ADDRESS),
                CLIENT_PORT);

        // Use DatagramSocket only in this scope:
        {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT_FOR_RESPONSES);
            int lengthOfAck = ackMessage.getBytes().length;
            int retries;

            for(retries = 0; retries < STREAM_INITIALIZATION_RETRIES; retries++) {
                try {
                    socket.send(datagramPacket);

                    // Get acknowledgements from clients.
                    // One packet must be received from each client.
                    for (int i = 0; i < Clients.getInstance().getClients().size(); i++) {
                        byte[] responseBuffer = new byte[lengthOfAck];
                        DatagramPacket receivedPacket = new DatagramPacket(responseBuffer,
                                responseBuffer.length);

                        // Todo: Make the response to the initialization
                        //socket.receive(receivedPacket);
                    }
                    // If it goes trough all clients, get out of the retries loop!
                    break;
                }
                catch(SocketTimeoutException timeoutException){
                    retries++;
                    if(retries >= STREAM_INITIALIZATION_RETRIES){
                        throw new StreamingInitFailedException("Not all clients did respond to the initialization.");
                    }
                }
                catch(Exception generalException){
                    Logger.getLogger(this.getClass().getName())
                            .log(Level.SEVERE, "Unknown error at stream init", generalException);
                }
            }

        }
    }

    /**
     * Send the finishedStreaming datagram.
     * This datagram indicates the client that the streaming of this part has ended.
     * @throws IOException
     */
    private void reportFinishedStreaming() throws IOException {
        byte[] data = StreamingMessage.STREAMING_FINALIZATION_MESSAGE.getBytes();

        DatagramPacket datagramPacket = new DatagramPacket(data,
                0,
                data.length,
                InetAddress.getByName(MULTICAST_GROUP_ADDRESS),
                CLIENT_PORT);

        // Use DatagramSocket only in this scope:
        {
            DatagramSocket socket = new DatagramSocket();
            socket.send(datagramPacket);
        }
    }

    /**
     * Starts the (multicast-)streaming of the song.
     */
    private void streamSong() throws IOException {
        DatagramSocket socket = new DatagramSocket();

        // Set this in order to increase UDP Reliability.
        // http://stackoverflow.com/questions/8267271/how-to-minimize-udp-packet-loss
        socket.setSendBufferSize(32 * 1024);
        socket.setTrafficClass(0x04);

        for(int i = 1; i < this.songCache.getExpectedCacheSize() + 1; i++){
            SongDatagram songDatagram = this.songCache.getSongDatagram(i);
            socket.send(songDatagram.getDatagramPacket());
        }
    }
}
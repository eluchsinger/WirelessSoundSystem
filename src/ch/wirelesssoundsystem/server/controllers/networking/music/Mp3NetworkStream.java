package ch.wirelesssoundsystem.server.controllers.networking.music;

import ch.wirelesssoundsystem.server.controllers.networking.NetworkStream;
import ch.wirelesssoundsystem.server.controllers.networking.Utility;
import ch.wirelesssoundsystem.server.models.songs.Song;
import ch.wirelesssoundsystem.shared.models.clients.Client;
import sun.net.util.IPAddressUtil;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Esteban Luchsinger on 03.12.2015.
 */
public class Mp3NetworkStream implements NetworkStream<Song> {

    DatagramSocket multicastSocket = new MulticastSocket();
    private final static int CLIENT_PORT = 6049;
    private final static int MAX_PACKET_SIZE = 700;

    public Mp3NetworkStream() throws IOException {
    }

    /**
     * Streams a song to the client.
     * @param song
     * @param client
     */
    public static void streamSong(Song song, Client client){
        File file = new File(song.getPath());
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] data = Files.readAllBytes(Paths.get(file.toURI()));

            // Calculate the amount of packets (as double).
            double calculatedAmountOfPackets = data.length / Mp3NetworkStream.MAX_PACKET_SIZE;
            int realAmountOfPackets = (int)calculatedAmountOfPackets;

            // Check if the amount of packets is an round integer.
            if(calculatedAmountOfPackets % 2 > 0){
                // if the amount of packets is not a round number, add one.
                calculatedAmountOfPackets++;
                realAmountOfPackets = (int)calculatedAmountOfPackets;
            }

            int offset = 0;

            // Send all packets.
            for(int i = 0; i < realAmountOfPackets; i++){

                // Get the smaller length. Either the rest of the data, if it is the last datagram,
                // or the MAX_PACKET_SIZE.
                int sendingLength = Math.min(data.length - offset, Mp3NetworkStream.MAX_PACKET_SIZE);
                DatagramPacket packet = new DatagramPacket(data, offset, sendingLength, client.getInetAddress(), Mp3NetworkStream.CLIENT_PORT);
                offset += sendingLength;
                socket.send(packet);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        //DatagramPacket datagramPacket = new DatagramPacket()
    }

    @Override
    public void startStream(Song data) {
    }

    @Override
    public void stopStream() {

    }
}

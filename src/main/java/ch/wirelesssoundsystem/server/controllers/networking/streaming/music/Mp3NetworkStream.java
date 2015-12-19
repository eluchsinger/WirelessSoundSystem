package ch.wirelesssoundsystem.server.controllers.networking.streaming.music;

import ch.wirelesssoundsystem.server.controllers.networking.NetworkStream;
import ch.wirelesssoundsystem.shared.models.networking.SongDatagram;
import ch.wirelesssoundsystem.shared.models.songs.Song;
import ch.wirelesssoundsystem.shared.models.clients.Client;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 03.12.2015.
 */
public class Mp3NetworkStream implements NetworkStream<Song> {

    DatagramSocket multicastSocket = new MulticastSocket();
    private final static int CLIENT_PORT = 6049;
    private final static int MAX_PACKET_SIZE = 700;
    private final static String MULTICAST_GROUP_ADDRESS = "239.255.42.100";

    public Mp3NetworkStream() throws IOException {
    }

    /**
     * Streams a song to the multicast group.
     *
     * @param song
     */
    public static void streamSong(Song song) {
        File file = new File(song.getPath());
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] data = Files.readAllBytes(Paths.get(file.toURI()));
            cacheData(data, InetAddress.getByName(MULTICAST_GROUP_ADDRESS), CLIENT_PORT);

            // Calculate the amount of packets (as double).
            double calculatedAmountOfPackets = data.length / (double) Mp3NetworkStream.MAX_PACKET_SIZE;
            int realAmountOfPackets = (int) calculatedAmountOfPackets;

            // Check if the amount of packets is an integer (no decimals).
            if (calculatedAmountOfPackets - (int) calculatedAmountOfPackets > 0) {
                // if the amount of packets is not a round number, add one.
                calculatedAmountOfPackets++;
                realAmountOfPackets = (int) calculatedAmountOfPackets;
            }

            int offset = 0;
            System.out.println("Amount of Packets: " + realAmountOfPackets);

            // Todo: Initiate the streaming: Send amount of packets (and/or size of file).

            // Send all packets.
            for (int i = 0; i < realAmountOfPackets; i++) {

                // Get the smaller length. Either the rest of the data, if it is the last datagram,
                // or the MAX_PACKET_SIZE.
                int sendingLength = Math.min(data.length - offset, Mp3NetworkStream.MAX_PACKET_SIZE);
                //DatagramPacket packet = new DatagramPacket(data, offset, sendingLength, client.getInetAddress(), Mp3NetworkStream.CLIENT_PORT);
                DatagramPacket packet = new DatagramPacket(data,
                        offset,
                        sendingLength,
                        InetAddress.getByName(Mp3NetworkStream.MULTICAST_GROUP_ADDRESS),
                        Mp3NetworkStream.CLIENT_PORT);
                offset += sendingLength;
                socket.send(packet);
                // Todo: What am I gonna do with the Sleep()...
//                Thread.sleep(1);
            }
        } catch (IOException e) {
            Logger.getLogger(Mp3NetworkStream.class.getName()).log(Level.WARNING, null, e);
        }
    }

    private static void cacheData(byte[] data, InetAddress inetAddress, int port) {
        try {
            List<SongDatagram> datagrams = SongDatagram.createPackets(data, inetAddress, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startStream(Song data) {
    }

    @Override
    public void stopStream() {

    }
}

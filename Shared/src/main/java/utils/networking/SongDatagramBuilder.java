package utils.networking;

import models.networking.SongDatagram;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 14.02.2016.
 */
public class SongDatagramBuilder {

    /**
     * Creates a list of SongDatagrams using a byte array.
     * The array could be for example the file data.
     * The UDP Header data is initialized using null-values.
     * @param data Byte array containing the data of the datagrams.
     * @return Returns a list of SongDatagrams. They are already Ordered by SequenceNr.
     * @throws UnknownHostException Throws an exception if the Host (Destination address) is unknown.
     */
    public static List<SongDatagram> createPackets(byte[] data) throws UnknownHostException {
        return SongDatagramBuilder.createPackets(data, null, SongDatagram.PORT_NOT_INITIALIZED);
    }

    /**
     * Creates a list of SongDatagrams using a byte array.
     * The array is the data of the song file.
     * @param data Byte array containing the data of the datagrams.
     * @param inetAddress Destination Address in the UDP Datagram Header.
     * @param port Destination Port of the UDP Datagram.
     * @return Returns a list of SongDatagrams. They are already Ordered by SequenceNr.
     * @throws UnknownHostException Throws an exception if the Host (Destination address) is unknown.
     */
    public static List<SongDatagram> createPackets(byte[] data, InetAddress inetAddress, int port) throws UnknownHostException {
        if(data.length < 1){
            return new ArrayList<>();
        }

        // Get the total amount of packets needed...
        double exactNumberOfPackets = data.length / (double)SongDatagram.MAX_DATA_SIZE;

        // Check if it is a round number. If no: add one packet. (Make it a round number)
        if((exactNumberOfPackets - (int)exactNumberOfPackets) > 0){
            exactNumberOfPackets++;
        }

        // Cast to int in order to get the real amount of packets
        // (Can't send 1.3 packets! Must send 2!)
        int realNumberOfPackets = (int)exactNumberOfPackets;

        List<SongDatagram> datagrams = new ArrayList<>(realNumberOfPackets);

        for(int i = 0; i < realNumberOfPackets; i++){
            int offset = i * SongDatagram.MAX_DATA_SIZE;
            int realPacketSize = Math.min(SongDatagram.MAX_DATA_SIZE, (data.length - offset));
            byte[] dataForPacket = new byte[realPacketSize];
            System.arraycopy(data,
                    offset,
                    dataForPacket,
                    0,
                    dataForPacket.length);

            SongDatagram newPacket;
            if(inetAddress == null){
                newPacket = new SongDatagram(dataForPacket);
            }
            else {
                newPacket = new SongDatagram(dataForPacket,
                        inetAddress,
                        port);
            }

            newPacket.setSequenceNumber(i + 1);
            datagrams.add(newPacket);
        }

        return datagrams;
    }

    /**
     * Converts a DatagramPacket into a SongDatagram.
     * Transforms the first SongDatagram.HEADER_SIZE Bytes into the header and
     * the rest into the datagram data.
     * @param packet Original DatagramPacket.
     * @return Returns a SongDatagram with Header and Data.
     */
    public static SongDatagram convertToSongDatagram(DatagramPacket packet){

        SongDatagram songDatagram = null;
        try {
            ByteBuffer completeBuffer = ByteBuffer.wrap(packet.getData());
            // Allocate bytes for the header data.
            byte[] headerData = new byte[SongDatagram.HEADER_SIZE];
            // Get the header data
            ByteBuffer headerBuffer = completeBuffer.get(headerData, 0, SongDatagram.HEADER_SIZE);
            // Get the sequence nr
            int sequenceNr = headerBuffer.getInt(0);
            // Get the length
            int length = headerBuffer.getInt(4) - SongDatagram.HEADER_SIZE;

            // Get the song data (the data without the header).
            byte[] songData = new byte[length];
            ByteBuffer dataBuffer = ByteBuffer.wrap(packet.getData(), SongDatagram.HEADER_SIZE, length);
            dataBuffer.get(songData);

            songDatagram = new SongDatagram(songData, packet.getAddress(), packet.getPort());
            songDatagram.setSequenceNumber(sequenceNr);
        }
        catch(Exception e){
            Logger.getLogger(SongDatagram.class.getName()).log(Level.SEVERE, "Packet corrupted", e);
        }
        return songDatagram;
    }
}
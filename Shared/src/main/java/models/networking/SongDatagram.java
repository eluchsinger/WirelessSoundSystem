package models.networking;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Esteban Luchsinger on 16.12.2015.
 * This datagram contains a part of the complete song.
 * It is only some bytes long and a song is usually composed by
 * hundreds or thousands of SongDatagrams.
 */
public class SongDatagram {
    public static final int SEQUENCE_NUMBER_NOT_INITIALIZED = -1;

    /**
     * Max Size of the data-section of this datagram (in bytes).
     */
    public static final int MAX_DATA_SIZE = 700;

    /**
     * The total header size of this datagram (in bytes).
     * This size is static and always the same.
     */
    public static final int HEADER_SIZE = 8;

    /**
     * The maximum size of this datagram.
     * (Composes using the MAX_DATA_SIZE and the HEADER_SIZE)
     */
    public static final int MAX_TOTAL_SIZE = HEADER_SIZE + MAX_DATA_SIZE;

    /**
     * Song Data of this datagram in bytes.
     */
    private final byte[] data;

    private InetAddress inetAddress;
    private int port;
    private SongDatagramHeader songDatagramHeader;

    /**
     * Initializes a SongDatagram.
     * @param data The data (bytes) that corresponds to this SongDatagram.
     * @param inetAddress The destination Address of this SongDatagram.
     * @param port The destination port of this SongDatagram.
     * @throws UnknownHostException is thrown if the destination host is unknown.
     */
    public SongDatagram(byte[] data, InetAddress inetAddress, int port) throws UnknownHostException {
        if(data.length > SongDatagram.MAX_DATA_SIZE){
            throw new IllegalArgumentException("Maximum Data for datagram exceeded!");
        }
        else{
            this.data = data.clone();
            if(inetAddress != null)
                this.inetAddress = InetAddress.getByAddress(inetAddress.getAddress());
            this.port = port;
            this.songDatagramHeader = new SongDatagramHeader(data);
        }
    }

    /**
     * Initializes a SongDatagram. Without destination host and port.
     * @param data The data (in bytes) that corresponds to this SongDatagram.
     * @throws UnknownHostException
     */
    public SongDatagram(byte[] data) throws UnknownHostException {
        this(data, null, -1);
    }

    /**
     * Gets the sequence nr. of this packet.
     * The sequence nr is the position of this packet inside of a stream of packets.
     * @return
     */
    public int getSequenceNr(){
        return this.songDatagramHeader.getSequenceNumber();
    }

    /**
     * Sets the sequence nr. of this packet.
     * @param sequenceNr
     */
    public void setSequenceNr(int sequenceNr){
        this.songDatagramHeader.setSequenceNumber(sequenceNr);
    }

    /**
     * @return Returns the InternetAddress of the destinationhost.
     */
    public InetAddress getInetAddress() {
        return inetAddress;
    }

    /**
     * Sets the destination address for this SongDatagram.
     * @param inetAddress
     */
    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    /**
     * @return Returns the destination port for this SongDatagram.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the destination port for this SongDatagram.
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Creates a NEW DatagramPacket. This packet includes the SongDatagramHeader and the data.
     * @return Returns a new DatagramPacket which can be used for the music streaming.
     */
    public DatagramPacket getDatagramPacket(){
        byte[] headerData = this.songDatagramHeader.toBytes();
//        byte[] totalData = new byte[headerData.length + this.data.length];


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(headerData);
            outputStream.write(this.data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] totalData = outputStream.toByteArray();

        return new DatagramPacket(totalData, totalData.length, this.inetAddress, this.port);
    }

    /**
     * Return the song data without header. Just. the. song. data.
     * @return
     */
    public byte[] getSongData(){
        return this.data;
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
            songDatagram.setSequenceNr(sequenceNr);
        }
        catch(Exception e){
            Logger.getLogger(SongDatagram.class.getName()).log(Level.SEVERE, "Packet corrupted", e);
        }
        return songDatagram;
    }


    /**
     * Creates a list of SongDatagrams using a byte array.
     * The array could be for example the file data.
     * The UDP Header data is initialized using null-values.
     * @param data Byte array containing the data of the datagrams.
     * @return Returns a list of SongDatagrams. They are already Ordered by SequenceNr.
     * @throws UnknownHostException Throws an exception if the Host (Destination address) is unknown.
     */
    public static List<SongDatagram> createPackets(byte[] data) throws UnknownHostException {
        return SongDatagram.createPackets(data, null, -1);
    }

    /**
     * Creates a list of SongDatagrams using a byte array.
     * The array could be for example the file data.
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

        // Check if it is a decimal number. If yes add one packet.
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

            newPacket.setSequenceNr(i + 1);
            datagrams.add(newPacket);
        }

        return datagrams;
    }


    /**
     * This class describes the header of a SongDatagram
     */
    private class SongDatagramHeader {
        private int sequenceNumber;
        private byte[] data;


        public SongDatagramHeader(byte[] data, int sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
            this.data = data;
        }

        /**
         * Initializes the SongDatagramHeader with the data and the sequence-number -1.
         * @param data
         */
        public SongDatagramHeader(byte[] data){
            this(data, -1);
        }

        public int getSequenceNumber(){
            return this.sequenceNumber;
        }

        public void setSequenceNumber(int sequenceNumber){
            this.sequenceNumber = sequenceNumber;
        }

        /**
         * Returns a calculated size of the datagram header and data.
         * @return
         */
        public int getDatagramSize(){
            return this.data.length + SongDatagram.HEADER_SIZE;
        }


        public byte[] toBytes(){
            // Create byte array containing the sequenceNr and the datagram size.
            byte[] bytes = ByteBuffer.allocate(8)
                    .putInt(this.sequenceNumber)
                    .putInt(this.getDatagramSize())
                    .array();
            return bytes;
        }
    }
}
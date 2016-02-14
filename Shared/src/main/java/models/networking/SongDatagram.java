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
     * The value of the port when it's not initialized.
     */
    public static final int PORT_NOT_INITIALIZED = -1;

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
            this.songDatagramHeader = new SongDatagramHeader(this);
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

    public SongDatagramHeader getSongDatagramHeader(){
        return this.songDatagramHeader;
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




}
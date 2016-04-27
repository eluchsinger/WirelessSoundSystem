package models.networking;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * <pre>
 * Created by Esteban Luchsinger on 16.12.2015.
 * This datagram contains a part of the complete song.
 * It is only some bytes long and a song is usually composed by
 * hundreds or thousands of SongDatagrams.
 * </pre>
 */
public class SongDatagram implements Comparable{

    //region CONSTANTS

    /**
     * First possible sequence number used on caches and streams.
     */
    public static final int FIRST_SEQUENCE_NUMBER = 1;

    /**
     * Max Size of the data-section of this datagram (in bytes).
     */
    public static final int MAX_DATA_SIZE = 700;

    /**
     * The total header size of this datagram (in bytes).
     * This size is static and always the same.
     * (Sequence Nr (int) and Length (int)).
     */
    public static final int HEADER_SIZE = Integer.BYTES * 2;

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
     * The value of the sequence nr when it is not initialized.
     */
    public static final int SEQUENCE_NR_NOT_INITIALIZED = -1;
    //endregion

    /**
     * Song Data (only song data, not header etc.) of this datagram in bytes.
     */
    private final byte[] data;

    /**
     * Sequence Number of this SongDatagram.
     * The sequence number is assigned to every single SongDatagram.
     * It defines the SongDatagram position in a stream.
     */
    private int sequenceNumber = SEQUENCE_NR_NOT_INITIALIZED;

    /**
     * Inet destination address of this datagram.
     */
    private InetAddress inetAddress;

    /**
     * Port destination address of this datagram.
     */
    private int port;

    /**
     * Initializes a SongDatagram. (The SequenceNr will be
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

            // Get the InetAddress (copy, not by reference)
            if(inetAddress != null)
                this.inetAddress = InetAddress.getByAddress(inetAddress.getAddress());
            this.port = port;
        }
    }

    /**
     * Initializes a SongDatagram. Without destination host and port.
     * @param data The data (in bytes) that corresponds to this SongDatagram.
     * @throws UnknownHostException
     */
    public SongDatagram(byte[] data) throws UnknownHostException {
        this(data, null, PORT_NOT_INITIALIZED);
    }


    /**
     * Gets the sequence nr. of this packet.
     * The sequence nr is the position of this packet inside of a stream of packets.
     * @return The Sequence Number of this packet.
     */
    public int getSequenceNumber(){
        return this.sequenceNumber;
    }

    /**
     * Sets the sequence nr. of this packet.
     * @param sequenceNr Desired Sequence Number.
     */
    public void setSequenceNumber(int sequenceNr){
        this.sequenceNumber = sequenceNr;
    }

    /**
     * @return Returns the InternetAddress of the destination host.
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
     * Creates a NEW DatagramPacket. This packet includes ALL data needed to be sent to the client.
     * @return Returns a new DatagramPacket which can be used for the music streaming.
     */
    public DatagramPacket getDatagramPacket(){

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(this.getHeaderByteData());
            outputStream.write(this.data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] totalData = outputStream.toByteArray();

        return new DatagramPacket(totalData, totalData.length, this.inetAddress, this.port);
    }

    /**
     * @return Return the song data without header. Just. the. song. data.
     */
    public byte[] getSongData(){
        return this.data;
    }

    /**
     * @return Return the header data of the SongDatagram.
     */
    public byte[] getHeaderByteData() {

        // Generate Header Byte Buffer.
        ByteBuffer headerData = ByteBuffer.allocate(SongDatagram.HEADER_SIZE);
        headerData.putInt(this.sequenceNumber);
        headerData.putInt(this.data.length);

        return headerData.array();
    }

    /**
     * The equals method observes only the SongDatagram data and sequence number.
     * @param o the comparing object
     * @return true if the compared objects are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongDatagram datagram = (SongDatagram) o;
        return sequenceNumber == datagram.sequenceNumber &&
                Arrays.equals(data, datagram.data);
    }

    /**
     * Generates the hash for the SongDatagram.
     * Observes only the song data and sequence number.
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(data, sequenceNumber);
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * The compared value is the sequence Number of the SongDatagram.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(Object o) {

        if (o == null)
            throw new NullPointerException("Compared Object can not be null.");

        if (o.getClass() != getClass())
            throw new ClassCastException("Compared object is not of the same class.");

        SongDatagram sd = (SongDatagram) o;

        if (sd.getSequenceNumber() == this.getSequenceNumber())
            return 0;
            // Return negative, if this object is less than the specified object.
        else if (this.getSequenceNumber() < sd.getSequenceNumber())
            return -1;
        // Else this object is greater than the specified object.
        else
            return 1;
    }
}
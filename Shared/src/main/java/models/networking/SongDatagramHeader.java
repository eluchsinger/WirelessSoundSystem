package models.networking;

/**
 * Created by Esteban Luchsinger on 14.02.2016.
 */

import java.nio.ByteBuffer;

/**
 * This class describes the header of a SongDatagram
 */
public class SongDatagramHeader {
    private int sequenceNumber;
    /**
     * This is the SongDatagram corresponding to the SongDatagramHeader.
     */
    private SongDatagram songDatagram;


    public SongDatagramHeader(SongDatagram songDatagram, int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        this.songDatagram = songDatagram;
    }

    /**
     * Initializes the SongDatagramHeader with the data and the sequence-number -1.
     * @param songDatagram
     */
    public SongDatagramHeader(SongDatagram songDatagram){
        this(songDatagram, -1);
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
        return this.songDatagram.getSongData().length + SongDatagram.HEADER_SIZE;
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
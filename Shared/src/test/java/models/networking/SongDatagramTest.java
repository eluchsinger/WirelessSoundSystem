package models.networking;

import models.songs.Song;
import org.junit.Test;

import java.net.DatagramPacket;
import java.net.InetAddress;

import static org.junit.Assert.*;

/**
 * Created by Esteban Luchsinger on 14.02.2016.
 */
public class SongDatagramTest {

    @Test
    public void testGetSequenceNr() throws Exception {

    }

    @Test
    public void testSetSequenceNr() throws Exception {

    }

    @Test
    public void testGetInetAddress() throws Exception {

    }

    @Test
    public void testSetInetAddress() throws Exception {

    }

    @Test
    public void testGetPort() throws Exception {
        SongDatagram datagram = new SongDatagram(new byte[10]);
        assertEquals(datagram.getPort(), SongDatagram.PORT_NOT_INITIALIZED);
    }

    @Test
    public void testSetPort() throws Exception {
        SongDatagram datagram = new SongDatagram(new byte[10]);
        assertEquals(datagram.getPort(), SongDatagram.PORT_NOT_INITIALIZED);

        datagram.setPort(1);
        assertEquals(datagram.getPort(), 1);
    }

    @Test
    public void testGetDatagramPacket() throws Exception {

    }

    @Test
    public void testGetSongData() throws Exception {

    }

    @Test
    public void testConvertToSongDatagram() throws Exception {

    }

    @Test
    public void testCreatePackets() throws Exception {

    }

    @Test
    public void testCreatePackets1() throws Exception {

    }

    /**
     * Tests if the actual header size matches the HEADER_SIZE constant.
     * @throws Exception
     */
    @Test
    public void testHeaderByteArraySize() throws Exception {
        SongDatagram datagram = new SongDatagram(new byte[10]);

        assertEquals(datagram.getHeaderByteData().length, SongDatagram.HEADER_SIZE);
    }

    /**
     * Tests the DatagramPacket creation of the SongDatagram.
     * @throws Exception
     */
    @Test
    public void testDatagramCreation() throws Exception {
        SongDatagram datagram = new SongDatagram(new byte[100], InetAddress.getLoopbackAddress(), 6060);
        DatagramPacket packet = datagram.getDatagramPacket();
        assertNotNull(packet);
    }

    /**
     * Tests the length of the created datagram and the theoretical length it should have
     * calculated off the header length and song data.
     * @throws Exception
     */
    @Test
    public void testDatagramTotalLength() throws Exception {
        SongDatagram datagram = new SongDatagram(new byte[100], InetAddress.getLoopbackAddress(), 6060);
        DatagramPacket packet = datagram.getDatagramPacket();
        assertEquals(packet.getLength(), datagram.getHeaderByteData().length + datagram.getSongData().length);
    }

    /**
     * If the maximum datagram size is exceeded, it should throw an exception.
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMaximumDatagramSize() throws Exception {
        new SongDatagram(new byte[SongDatagram.MAX_DATA_SIZE + 1]);
    }
}
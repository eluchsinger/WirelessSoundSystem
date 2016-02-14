package models.networking;

import org.junit.Test;

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
}
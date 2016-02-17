package utils.networking;

import models.networking.SongDatagram;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Esteban Luchsinger on 17.02.2016.
 */
public class SongDatagramBuilderTest {


    /**
     * Creates packets and checks the amount of packets created.
     * This test creates data for 10 packets. The data should have
     * EXACTLY space in the 10 packets.
     * @throws Exception
     */
    @Test
    public void testCreatePacketsCheckRoundAmount() throws Exception {
        int amountOfPackets = 50;
        byte[] data = new byte[SongDatagram.MAX_DATA_SIZE * amountOfPackets];
        List<SongDatagram> list = SongDatagramBuilder.createPackets(data);

        assertEquals(list.size(), amountOfPackets);
    }

    /**
     * Creates data that does not have space in an integer amount of packets.
     * The builder has to create an extra packet to put the last information in.
     * @throws Exception
     */
    @Test
    public void testCreatePacketsCheckAmountWithDecimals() throws Exception {
        int amountOfPackets = 50; // Actually, one more (because there is one byte too much)!
        byte[] data = new byte[SongDatagram.MAX_DATA_SIZE * amountOfPackets + 1];

        List<SongDatagram> list = SongDatagramBuilder.createPackets(data);

        assertEquals(list.size(), amountOfPackets + 1);
    }

    @Test
    public void testCreatePacketsWithInet() throws Exception {

    }

    @Test
    public void testCreatePacketsWithoutInet() throws Exception {
        int amountOfPackets = 50;
        byte[] data = new byte[SongDatagram.MAX_DATA_SIZE * amountOfPackets + 1];

        List<SongDatagram> list = SongDatagramBuilder.createPackets(data);

        for(int i = 0; i < list.size(); i++){
            SongDatagram sd = list.get(i);
            assertEquals(sd.getSequenceNumber(), i + 1);
            assertEquals(sd.getPort(), SongDatagram.PORT_NOT_INITIALIZED);
            assertNull(sd.getInetAddress());
            assertTrue(sd.getSongData().length <= SongDatagram.MAX_DATA_SIZE);
        }
    }

    @Test
    public void testConvertToSongDatagram() throws Exception {

    }
}
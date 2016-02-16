package models.networking;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Esteban Luchsinger on 16.02.2016.
 */
public class SongCacheTest {

    /**
     * Default caache size for the tests.
     * (Can still vary).
     */
    public final static int DEFAULT_CACHE_SIZE = 1000;
    @Test
    public void testAdd() throws Exception {

    }

    @Test
    public void testAdd1() throws Exception {

    }

    @Test
    public void testGetSongDatagram() throws Exception {

    }

    @Test
    public void testContains() throws Exception {

    }

    @Test
    public void testIsComplete() throws Exception {

    }

    @Test
    public void testGetExpectedCacheSize() throws Exception {

    }

    /**
     * Tests the getMissingSequenceNumbers with a complete cache.
     * @throws Exception
     */
    @Test
    public void testGetMissingSequenceNumbersCompleteCache() throws Exception {
        SongCache cache = new SongCache(DEFAULT_CACHE_SIZE);

        for(int i = SongDatagram.FIRST_SEQUENCE_NUMBER; i <= DEFAULT_CACHE_SIZE; i++){
            SongDatagram datagram = new SongDatagram(new byte[100]);
            datagram.setSequenceNumber(i);

            cache.add(datagram);
        }

        assertTrue(cache.isComplete());
    }

    @Test
    public void testCacheSong() throws Exception {

    }

    @Test
    public void testCacheSong1() throws Exception {

    }
}
package controllers.networking.streaming;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Esteban Luchsinger on 23.02.2016.
 */
public class MusicStreamingServiceTest {

    /**
     * Tries to get the instance of the MusicStreamingService Singleton.
     * @throws Exception
     */
    @Test
    public void testGetInstance() throws Exception {
        assertNull(MusicStreamingService.getInstance());
    }

    /**
     * Tests the start of the service.
     * @throws Exception
     */
    @Test
    public void testStartAfterInit() throws Exception {
        MusicStreamingService service = MusicStreamingService.getInstance();

        if(service.getCurrentServiceStatus() != MusicStreamingService.ServiceStatus.STOPPED)
        {
            service.stop();
        }

        assertEquals(service.getCurrentServiceStatus(), MusicStreamingService.ServiceStatus.STOPPED);

        service.start();

        assertEquals(service.getCurrentServiceStatus(), MusicStreamingService.ServiceStatus.RUNNING);

    }

    @Test
    public void testStop() throws Exception {

    }
}
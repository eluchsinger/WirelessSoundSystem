package controllers.networking.streaming;

import controllers.networking.streaming.music.ServiceStatus;
import controllers.networking.streaming.music.udp.UDPMusicStreamingService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Esteban Luchsinger on 23.02.2016.
 */
public class MusicStreamingServiceTest {

    /**
     * Tests the start of the service.
     * @throws Exception
     */
    @Test
    public void testStartAfterInit() throws Exception {
        UDPMusicStreamingService service = new UDPMusicStreamingService();

        if(service.getCurrentServiceStatus() != ServiceStatus.STOPPED)
        {
            service.stop();
        }

        assertEquals(service.getCurrentServiceStatus(), ServiceStatus.STOPPED);

        service.start();

        assertEquals(service.getCurrentServiceStatus(), ServiceStatus.WAITING);
    }

    @Test
    public void testStop() throws Exception {

    }
}
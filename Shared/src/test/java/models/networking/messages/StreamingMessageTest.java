package models.networking.messages;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Esteban Luchsinger on 26.02.2016.
 */
public class StreamingMessageTest {

    @Test
    public void testMakeMissingPacketsMessage() throws Exception {

        Integer[] arr = {3, 4, 5, 6, 7, 8};
        List<Integer> list = Arrays.asList(arr);

        String result = StreamingMessage.makeMissingPacketsMessage(list);
        assertEquals(result, StreamingMessage.MISSING_PACKETS_MESSAGE + "3,4,5,6,7,8");
    }

    @Test
    public void testParseMissingPacketsMessage() throws Exception {

        Integer[] arrInput = {3, 4, 5, 6, 7, 8};
        List<Integer> listInput = Arrays.asList(arrInput);

        String result = StreamingMessage.makeMissingPacketsMessage(listInput);
        System.out.println(result);

        List<Integer> listOutput = StreamingMessage.parseMissingPacketsMessage(result);

        // Check List<Integer> comparision!
        assertEquals(listInput, listOutput);
    }
}
package models.networking.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Esteban Luchsinger on 18.12.2015.
 * This class contains static messages used for the streaming network protocols
 */
public final class StreamingMessage {

    // INITIALIZATION
    public final static String STREAMING_INITIALIZATION_MESSAGE = "start:";
    public final static String STREAMING_INITIALIZATION_ACKNOWLEDGED_MESSAGE = "startack:";

    // QUALITY
    public final static String MISSING_PACKETS_MESSAGE = "missing:";

    // FINALIZATION
    public final static String STREAMING_FINALIZATION_MESSAGE = "finish:";
    public final static String STREAMING_FINALIZATION_ACKNOWLEDGED_MESSAGE = "finishack:";

    //region Initialization
    /**
     * The Streaming Initialization Message is sent before the server starts streaming a song.
     * It contains the amount of packets that are going to be sent.
     * @param amountOfPackets Amount of packets that are going to be sent in the stream.
     * @return Returns the correct message corresponding with the parameters.
     */
    public static String initializationMessage(int amountOfPackets){
        return StreamingMessage.STREAMING_INITIALIZATION_MESSAGE + amountOfPackets;
    }

    /**
     * The acknowledgement of the client to the server.
     * Indicates that an initialization message was received.
     * @param amountOfPackets Amount of packets that were announced to be sent.
     *                        (Same amount as in the Init Message!)
     * @return Returns the correct message corresponding with the given parameters.
     */
    public static String initializationAckMessage(int amountOfPackets){
        return StreamingMessage.STREAMING_INITIALIZATION_ACKNOWLEDGED_MESSAGE + amountOfPackets;
    }
    //endregion

    //region Quality

    /**
     * The missing packets message gets constructed here.
     * Example: missing:10,12,33,55
     * @param missingPackets List with the missing integers.
     * @return Returns a string with the missing packets.
     */
    public static String makeMissingPacketsMessage(List<Integer> missingPackets){
        StringBuilder builder = new StringBuilder();

        // Append the numbers in the string.
        missingPackets.stream()
                .forEach(integer -> builder.append(integer).append(","));

        // Delete the last comma, if needed.
        if(builder.length() > 0 && builder.charAt(builder.length() - 1) == ','){
            builder.deleteCharAt(builder.length() - 1);
        }

        return StreamingMessage.MISSING_PACKETS_MESSAGE + builder.toString();
    }

    /**
     * Parses the message: Gets a list with the missing packet-sequences.
     * @param message The missing packets message received.
     * @return Returns a list with the missing packet-sequences.
     */
    public static List<Integer> parseMissingPacketsMessage(String message){

        List<Integer> list = new ArrayList<>();
        if(message.startsWith(StreamingMessage.MISSING_PACKETS_MESSAGE)){

            // Get the data part of the message.
            String dataPart = message.substring(StreamingMessage.MISSING_PACKETS_MESSAGE.length());

            // Split the data-part of the message in the different integers.
            String[] splitParts = dataPart.split(",");

            // Fill the list.
            for(String s : splitParts){
                list.add(Integer.parseInt(s));
            }
        }

        return list;
    }
    //endregion

    //region Finalization
    public static String finalizationMessage(int amountOfPackets){
        return StreamingMessage.STREAMING_FINALIZATION_MESSAGE + amountOfPackets;
    }

    public static String finalizationAckMessage(int amountOfPackets){
        return StreamingMessage.STREAMING_FINALIZATION_ACKNOWLEDGED_MESSAGE + amountOfPackets;
    }
    //endregion
}

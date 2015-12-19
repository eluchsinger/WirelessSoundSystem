package ch.wirelesssoundsystem.shared.models.networking.messages;

import java.util.List;

/**
 * Created by Esteban Luchsinger on 18.12.2015.
 * This class contains static messages used for the streaming network protocols
 */
public final class StreamingMessage {

    // INITIALIZATION
    private final static String STREAMING_INITIALIZATION_MESSAGE = "start:";
    private final static String STREAMING_INITIALIZATION_ACKNOWLEDGED_MESSAGE = "startack:";

    // QUALITY
    private final static String MISSING_PACKETS_MESSAGE = "missing:";

    // FINALIZATION
    private final static String STREAMING_FINALIZATION_MESSAGE = "finish:";
    private final static String STREAMING_FINALIZATION_ACKNOWLEDGED_MESSAGE = "finishack:";

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
    public static String missingPacketsMessage(List<Integer> missingPackets){
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

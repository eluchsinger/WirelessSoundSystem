package ch.wirelesssoundsystem.shared.models.networking.messages;

/**
 * Created by Esteban Luchsinger on 18.12.2015.
 * This class contains static messages used for the streaming network protocols
 */
public final class StreamingMessage {

    private final static String STREAMING_INITIALIZATION_MESSAGE = "start:";
    private final static String STREAMING_INITIALIZATION_ACKNOWLEDGED_MESSAGE = "startack:";

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
}

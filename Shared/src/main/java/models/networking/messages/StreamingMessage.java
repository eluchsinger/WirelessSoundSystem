package models.networking.messages;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by Esteban Luchsinger on 18.12.2015.
 * This class contains static messages used for the streaming network protocols
 *
 * Change 04.03.2016 (ELU):
 * Changed the messages to be used in a continuous stream. (TCP is continious, UDP is Datagram-oriented).
 */
public final class StreamingMessage {

    // INITIALIZATION example: <start length=100>
    public final static String STREAMING_INITIALIZATION_MESSAGE = "<stream>";
    public final static String STREAMING_INITIALIZATION_LENGTH_ATTRIBUTE = "length";
    public final static String STREAMING_INITIALIZATION_ACKNOWLEDGED_MESSAGE = "<ack>stream</ack>";

    // QUALITY
    public final static String MISSING_PACKETS_MESSAGE = "<missing>";
    public final static String MISSING_PACKETS_CLOSE = "</missing>";

    // FINALIZATION
    public final static String STREAMING_FINALIZATION_MESSAGE = "</stream>";

    //region Initialization
    /**
     * The Streaming Initialization Message is sent before the server starts streaming a song.
     * It contains the amount of packets for UDP and bytes for TCP that are going to be sent.
     * Example: <start length=100>
     * @param amountOfPackets Amount of packets that are going to be sent in the stream.
     * @return Returns the correct message corresponding with the parameters.
     */
    public static String initializationMessage(int amountOfPackets) {
        return setAttribute(STREAMING_INITIALIZATION_MESSAGE,
                STREAMING_INITIALIZATION_LENGTH_ATTRIBUTE,
                Integer.toString(amountOfPackets));
    }

    /**
     * The message sent when the streaming finished.
     * Example: </stream>
     * @return
     */
    public static String streamingEndedMessage() {
        return STREAMING_FINALIZATION_MESSAGE;
    }

    /**
     * Sets an attribute into the tag.
     * @param tag original tag
     * @param attribute Attribute (example: length)
     * @param value Value of the attribute
     * @return Returns a new tag (String) with the new attribute.
     */
    public static String setAttribute(String tag, String attribute, String value) {

        if(tag.isEmpty() || attribute.isEmpty() || value.isEmpty()){
            throw new IllegalArgumentException("One of the parameters is empty.");
        }

        String returnString;
        // If the attribute already exists, change it.
        if(tag.contains(attribute)){
            // Just delete the attribute with the old value and make a new one.
            // Example of Regex (_ = Whitespace): _<attribute>=[0-9]* --> Finds the attribute and removes it.
            tag = tag.replaceFirst(" " + attribute + "=[0-9]*", "");
        }

        // If the attribute doesn't exist, add it. (--> If the attribute existed before, it was deleted!)
        StringBuilder builder = new StringBuilder(tag);
        String insertText = " " + attribute + "=" + value;

        if (tag.startsWith("<") && tag.endsWith(">")) {
            // Insert at last position possible.
            builder.insert(tag.length() - 1, insertText);
        }

        returnString = builder.toString();

        return returnString;
    }

    /**
     * Returns a string list with all attributes (with values) in the tag.
     * Example:
     *  Input: <start length=100 size=300>
     *  Output: ["length=100", "size=300"]
     * @param tag
     * @return
     */
    public static List<String> getAttributes(String tag){
        Pattern pattern = Pattern.compile("(?<attribute>[a-zA-Z]*=(?<value>[0-9]*))");

        List<String> attributes = new ArrayList<>();
        Matcher matcher = pattern.matcher(tag);

        while(matcher.find()){
            attributes.add(matcher.group("attribute"));
        }
        return attributes;
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

        builder.append(StreamingMessage.MISSING_PACKETS_MESSAGE);

        // Append the numbers in the string.
        missingPackets.stream()
                .forEach(integer -> builder.append(integer).append(" "));

        builder.append(StreamingMessage.MISSING_PACKETS_CLOSE);

        return builder.toString();
    }

    /**
     * Parses the message: Gets a list with the missing packet-sequences.
     * @param message The missing packets message received.
     * @return Returns a list with the missing packet-sequences.
     */
    public static List<Integer> parseMissingPacketsMessage(String message){

        List<Integer> list = new ArrayList<>();

        if(message.startsWith(StreamingMessage.MISSING_PACKETS_MESSAGE)
                && message.endsWith(StreamingMessage.MISSING_PACKETS_CLOSE)){
            Matcher matcher =  Pattern.compile("\\d+").matcher(message);

            while(matcher.find()){
                String nextMatch = matcher.group();
                list.add(Integer.parseInt(nextMatch));
            }
        }
        return list;
    }
    //endregion

    //region Finalization

    //endregion
}

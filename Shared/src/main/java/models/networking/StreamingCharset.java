package models.networking;

/**
 * Created by Esteban Luchsinger on 08.03.2016.
 * Charsets used for streaming.
 */
public class StreamingCharset {
    public final static String US_ASCII = "US-ASCII";

    /**
     * Default charset used in WSS Streaming.
     * Current default is US-ASCII (because of Base64 encoding).
     */
    public final static String DEFAULT_CHARSET = US_ASCII;


}

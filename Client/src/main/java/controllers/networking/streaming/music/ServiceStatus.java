package controllers.networking.streaming.music;

/**
 * <pre>
 * Created by Esteban Luchsinger on 29.02.2016.
 * Enumerates the service status.
 * </pre>
 */
public enum ServiceStatus {
    /**
     * The Streaming Service is running and waiting for a stream initialization.
     */
    WAITING,
    /**
     * The Streaming Service is receiving an initialized stream.
     */
    RECEIVING,

    /**
     * The streaming service received a song.
     */
    READY,
    /**
     * The Streaming Service is not running.
     */
    STOPPED
}

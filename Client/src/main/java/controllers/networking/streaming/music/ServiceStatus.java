package controllers.networking.streaming.music;

/**
 * Created by Esteban Luchsinger on 29.02.2016.
 * Enumerates the service status.
 */
public enum ServiceStatus {
    /**
     * The Streaming Service is running and waiting for a stream initialization.
     */
    READY,
    /**
     * The Streaming Service is receiving an initialized stream.
     */
    RECEIVING,
    /**
     * The Streaming Service is not running.
     */
    STOPPED
}

package controllers.statistics;

/**
 * Created by Esteban Luchsinger on 14.03.2016.
 */
public enum StatisticsMode {
    /**
     * Relative mode is currently optimized for UDP.
     * It shows the relative amount (in %) of the received PACKETS.
     */
    RELATIVE,

    /**
     * Absolute mode is currently optimized for TCP.
     * It shows the absolute amount of data (bytes) received.
     */
    ABSOLUTE
}

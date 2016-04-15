package controllers.networking;

/**
 * <pre>
 * Created by Esteban Luchsinger on 03.12.2015.
 * </pre>
 */
public interface NetworkStream<T> {
    void startStream(T data);
    void stopStream();
}

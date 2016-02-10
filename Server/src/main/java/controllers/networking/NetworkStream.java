package controllers.networking;

/**
 * Created by Esteban Luchsinger on 03.12.2015.
 */
public interface NetworkStream<T> {
    void startStream(T data);
    void stopStream();
}

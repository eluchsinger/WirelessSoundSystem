package models.networking.exceptions;

/**
 * Created by Esteban Luchsinger on 18.12.2015.
 * This exception is called, if the initialization of the streaming failed.
 */
public class StreamingInitFailedException extends Exception {

    public StreamingInitFailedException(){
        super();
    }

    public StreamingInitFailedException(String text){
        super(text);
    }
}

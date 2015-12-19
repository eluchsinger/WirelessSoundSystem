package ch.wirelesssoundsystem.shared.models.networking.exceptions;

/**
 * Created by Esteban Luchsinger on 18.12.2015.
 */
public class CacheOverflowException extends Exception {

    public CacheOverflowException(String text){
        super(text);
    }
}

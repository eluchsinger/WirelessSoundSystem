package models.networking.dtos;

import java.io.Serializable;

/**
 * Created by Esteban Luchsinger on 17.03.2016.
 * The command "Play". It tells the client to play the song.
 */
public class PlayCommand implements Serializable {
    private static final long serialVersionUID = -3809733967947659045L;

    public final String title;
    public final String artist;

    public PlayCommand(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }
}

package models.networking.dtos;

import java.io.Serializable;

/**
 * Created by Esteban Luchsinger on 17.03.2016.
 * The command "Play". It tells the client to play the song.
 */
public class PlayCommand implements Serializable {
    private static final long serialVersionUID = -3809733967947659045L;

    public String songTitle;
    public String artist;
    public byte[] data;

    public PlayCommand(byte[] data) { this("", "", data); }

    public PlayCommand(String songTitle, byte[] data) { this(songTitle, "", data); }

    public PlayCommand(String songTitle, String artist, byte[] data) {

        this.songTitle = songTitle;
        this.artist = artist;
        this.data = data;
    }
}

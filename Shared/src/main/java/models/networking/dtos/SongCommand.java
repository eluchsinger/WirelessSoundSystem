package models.networking.dtos;

import java.io.Serializable;

/**
 * Created by Esteban Luchsinger on 17.03.2016.
 */
public class SongCommand implements Serializable {
    private static final long serialVersionUID = -3809733967947659045L;

    public byte[] data;

    public SongCommand(byte[] data) {
        this.data = data;
    }
}

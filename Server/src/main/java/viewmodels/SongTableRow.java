package viewmodels;

import javafx.scene.control.TableRow;
import models.songs.PlayableSong;

/**
 * <pre>
 * Created by Esteban Luchsinger on 19.04.2016.
 * </pre>
 */
public class SongTableRow extends TableRow<PlayableSong> {
    private static final String PLAYING_STYLE = "song-is-playing";


    public SongTableRow() {
        super();
    }

    @Override
    protected void updateItem(PlayableSong item, boolean empty) {
        super.updateItem(item, empty);

        /**
         * Handles the setting and removing of the style class, depending on the isPlaying Property of the song.
         */
        if(empty) {
            this.getStyleClass().clear();
        } else {
            item.isPlayingProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    this.getStyleClass().add(PLAYING_STYLE);
                } else {
                    this.getStyleClass().removeAll(PLAYING_STYLE);
                }
            });
        }
    }
}

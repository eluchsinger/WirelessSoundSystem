package viewmodels;

import javafx.scene.control.TableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viewmodels.songs.PlayableSong;

/**
 * <pre>
 * Created by Esteban Luchsinger on 19.04.2016.
 * </pre>
 */
public class SongTableRow extends TableRow<PlayableSong> {
    private final Logger logger;
    private static final String PLAYING_STYLE = "song-is-playing";


    public SongTableRow() {
        super();
        this.logger = LoggerFactory.getLogger(this.getClass());

        this.logger.info("Row Item: " + this.getItem());
    }

    @Override
    protected void updateItem(PlayableSong item, boolean empty) {
        super.updateItem(item, empty);

        /**
         * Handles the setting and removing of the style class, depending on the isPlaying Property of the song.
         */
        if(empty) {
            this.getStyleClass().clear();
            this.logger.info("Removed StyleClass " + item);
        } else {
            item.isPlayingProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    this.getStyleClass().add(PLAYING_STYLE);
                    this.logger.info("Updated StyleClass " + item);
                } else {
                    this.getStyleClass().clear();
                    this.logger.info("Removed StyleClass " + item);
                }
            });
        }
    }
}

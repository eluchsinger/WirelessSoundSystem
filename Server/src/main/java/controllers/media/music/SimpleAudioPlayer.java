package controllers.media.music;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import models.songs.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.exceptions.NotImplementedException;

import java.io.File;
import java.util.Optional;

/**
 * <pre>
 * Created by Esteban Luchsinger on 01.12.2015.
 * This is a simple audio player designed to handle the music playing.
 *
 * If you need to use this MediaPlayer for streaming, you should evaluate using the <code>NetworkAudioPlayer</code>.
 * </pre>
 */
public class SimpleAudioPlayer implements controllers.media.MediaPlayer<Song> {

    private final Logger logger;
    /**
     * Access only with the Accessor-Methods!
     */
    private MediaPlayer mediaPlayer;

    private BooleanProperty isPlaying;
    private ObjectProperty<Duration> currentMediaTime;
    private StringProperty currentMediaTimeString;
    private ObjectProperty<Duration> totalMediaDuration;
    private DoubleProperty volume;

    private ObservableList<Song> playlist;
    private Song lastPlayed;

    /**
     * Default Constructor
     */
    public SimpleAudioPlayer() {
        // Call parameterized constructor with a new list.
        this(FXCollections.observableArrayList());
    }

    /**
     * Constructor with a song list. This will be the playlist.
     * @param songs Songs for the playlist.
     */
    public SimpleAudioPlayer(ObservableList<Song> songs) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.playlist = songs;

        this.isPlaying = new SimpleBooleanProperty();
        this.currentMediaTime = new SimpleObjectProperty<>();
        this.currentMediaTimeString = new SimpleStringProperty();
        this.totalMediaDuration = new SimpleObjectProperty<>();
        this.volume = new SimpleDoubleProperty(1);
    }

    /**
     * Plays the selected track if found and / or resumes it.
     * @param track The track that should be played. (Must not be the same reference as in the playlist).
     * @param tryResume Tries to resume the song, if it is paused.
     */
    public void play(Song track, boolean tryResume) {

        // Only do something if the playlist is not empty.
        if (this.playlist.size() > 0) {
            boolean needNewMediaPlayer = true;
            if (this.getMediaPlayer() != null
                    && this.getMediaPlayer().getStatus() != MediaPlayer.Status.DISPOSED) {

                // Check if the currentTrack is the same track and paused (and tryResume == true).
                if (tryResume && this.getCurrentTrack() != null && this.getCurrentTrack().equals(track)) {

                    // Resume the paused track.
                    this.getMediaPlayer().play();
                    needNewMediaPlayer = false;
                }
            }

            // There has to be a new MediaPlayer.
            if (needNewMediaPlayer) {

                // Stop the player, regardless of state.
                this.stop();
                Song song = this.findSongFromPlaylist(track);

                if (song != null) {
                    Media media = this.createMediaFromSong(song);
                    this.setMediaPlayer(new MediaPlayer(media));
                    this.getMediaPlayer().play();
                    this.lastPlayed = song;
                }
            }
        }
    }

    @Override
    public void play(Song track) {
        this.play(track, false);
    }

    @Override
    public void pause() {
        if (this.getMediaPlayer() != null) {
            this.getMediaPlayer().pause();
        }
    }

    @Override
    public void stop() {
        // Check, if the MediaPlayer needs to get disposed first.
        if (this.getMediaPlayer() != null) {
            this.getMediaPlayer().dispose();
            this.setMediaPlayer(null);
        }
    }

    @Override
    public boolean isPlaying() {
        if (this.getMediaPlayer() == null || this.getMediaPlayer().getStatus() == null) {
            return false;
        }

        switch (this.getMediaPlayer().getStatus()) {
            case PLAYING:
                return true;
            default:
                return false;
        }
    }

    @Override
    public BooleanProperty isPlayingProperty() {
        return this.isPlaying;
    }

    @Override
    public Song getNextTrack() {
        if (this.lastPlayed != null) {
            Song song = null;

            try{
                int index = this.playlist.lastIndexOf(this.lastPlayed);

                if(index < this.playlist.size() - 1){
                    song = this.playlist.get(index + 1);
                }
            } catch(Exception ignored){ }

            return song;
        }
        return null;
    }

    @Override
    public boolean playNextTrack() {
        throw new NotImplementedException();
    }

    @Override
    public Song getPreviousTrack() {
        if (this.lastPlayed != null) {
            Song song = null;
            try {
                int index = this.playlist.lastIndexOf(this.lastPlayed);

                if (index > 0) {
                    song = this.playlist.get(index - 1);
                }
            } catch (Exception ignored) { }

            return song;
        }
        else {
            return null;
        }
    }

    @Override
    public boolean playPreviousTrack() {
        throw new NotImplementedException();
    }

    @Override
    public Song getCurrentTrack() {
        if (this.getMediaPlayer() == null) {
            return null;
        } else {
            String uri = this.getMediaPlayer().getMedia().getSource();

            // Todo: Evaluate parallelStream().
            Optional<Song> maybeSong = this.playlist.stream()
                    .filter(song -> new File(song.getPath()).toURI().toString().equals(uri))
                    .findFirst();


            // Return the present song or null.
            return maybeSong.orElse(null);
        }
    }

    @Override
    public SimpleObjectProperty<Song> currentTrackProperty() {
        return null;
    }

    @Override
    public double getVolume() {
        return this.volumeProperty().get();
    }

    @Override
    public void setVolume(double value) {
        this.volumeProperty().set(value);
    }

    @Override
    public DoubleProperty volumeProperty() {
        return volume;
    }

    private Media createMediaFromSong(Song song) {

        // Have to create a temporary file to convert the path to a URI.
        File tempFile = new File(song.getPath());

        Media media = new Media(tempFile.toURI().toString());
        return media;
    }

    /**
     * Finds the next song using the lastPlayed song.
     *
     * @return Returns the next song in the playlist, returns the first song if a next song could not be found.
     */
    private Song findNextSong() {
        Optional<Song> optSong = this.playlist.stream().filter(s -> s.equals(this.lastPlayed)).findFirst();

        if (optSong.isPresent()) {
            return optSong.get();
        } else {
            this.logger.warn("Next Song was not found! Using first song!");
            return this.playlist.get(0);
        }
    }

    /**
     * Finds the reference to a song in the playlist.
     *
     * @param song Song with external reference (not in the playlist).
     * @return Returns a reference to the equivalent song in the playlist. If no equal song was found in the playlist, return NULL.
     */
    private Song findSongFromPlaylist(Song song) {

        // Todo: Evaluate parallelStream().
        return this.playlist.stream()
                .filter(s -> s.equals(song))
                .findFirst()
                .orElse(null);
    }

    public ObjectProperty<Duration> currentMediaTimeProperty() {
        return this.currentMediaTime;
    }

    public ObjectProperty<Duration> totalMediaDurationProperty() {
        return this.totalMediaDuration;
    }

    private void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;

        if (this.getMediaPlayer() != null) {
            this.bindProperties(this.getMediaPlayer());
        }
    }

    private MediaPlayer getMediaPlayer() {
        return this.mediaPlayer;
    }

    /**
     * This method handles the binding properties of the MediaPlayer.
     * Use only ONCE per MediaPlayer!
     *
     * @param player
     */
    private void bindProperties(MediaPlayer player) {

        if (this.getMediaPlayer() != null) {
            this.currentMediaTime.bind(this.getMediaPlayer().currentTimeProperty());
            this.totalMediaDuration.bind(this.getMediaPlayer().totalDurationProperty());

            // Volume property.
            this.getMediaPlayer().setVolume(this.getVolume());
            this.volume.bindBidirectional(this.getMediaPlayer().volumeProperty());


            // Add Listener for the isPlaying property.
            // Maps the different MediaPlayer.Status to the boolean value isPlaying.
            player.statusProperty().addListener((observable, oldValue, newValue) -> {
                if (oldValue == MediaPlayer.Status.PLAYING || newValue == MediaPlayer.Status.PLAYING) {
                    if (newValue == MediaPlayer.Status.PLAYING)
                        this.isPlayingProperty().set(true);
                    else
                        this.isPlayingProperty().set(false);
                }
            });
        }
    }

    private String formatDuration(Duration duration) {
        return (int) duration.toMinutes() + ":" + (int) (duration.toSeconds() % 60);
    }
}

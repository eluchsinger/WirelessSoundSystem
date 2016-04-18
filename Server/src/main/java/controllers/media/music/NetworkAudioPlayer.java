package controllers.media.music;

import controllers.networking.streaming.music.MusicStreamController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import models.songs.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * <pre>
 * Created by Esteban Luchsinger on 13.04.2016.
 * The simple audio player enhanced for use in network. This means that this controller will
 * handle the streaming of the right songs with the clients.
 * The network clients are controlled by the required music stream controller.
 * </pre>
 */
public class NetworkAudioPlayer extends BaseAudioPlayer implements controllers.media.MediaPlayer<Song> {
    //region Members

    private final Logger logger;

    private final MusicStreamController musicStreamController;
    private MediaPlayer mediaPlayer;

    //endregion Members

    //region Constructors

    /**
     * Calls the advanced constructor. Initialized with an empty song list.
     * @param musicStreamController MusicStreamController used to use all the
     */
    public NetworkAudioPlayer(MusicStreamController musicStreamController) {
        this(FXCollections.observableArrayList(), musicStreamController);
    }

    /**
     * Advanced Constructor with parameters.
     * @param songs
     * @param musicStreamController
     */
    public NetworkAudioPlayer(ObservableList<Song> songs, MusicStreamController musicStreamController) {
        super(songs);
        this.logger = LoggerFactory.getLogger(this.getClass());

        this.musicStreamController = musicStreamController;
    }

    //endregion Constructors

    //region Getters & Setters

    /**
     * Initializes the media player for the song in the parameters.
     * If the current MediaPlayer already uses the song in the parameters, it wont be reinitialized.
     * If the current MediaPlayer is playing and the Song object is different, it will be stopped and disposed.
     * @param song Song for the MediaPlayer
     * @return the MediaPlayer for the desired song.
     */
    private MediaPlayer getMediaPlayer(Song song) {
        Media possibleMedia = this.createMediaFromSong(song);
        if(this.mediaPlayer == null) {
            this.mediaPlayer = this.initializeMediaPlayer(possibleMedia);
        } else {
            // Check if the current MediaPlayer is playing the correct song.
            if (!possibleMedia.getSource().equals(this.mediaPlayer.getMedia().getSource())) {

                // If it's not the correct song, initialize new MediaPlayer.
                this.mediaPlayer = this.initializeMediaPlayer(possibleMedia);
            }
        }

        this.currentTrackProperty().setValue(song);

        return this.mediaPlayer;
    }

    /**
     * Retrieves the current MediaPlayer with whatever song it may have initialized (null is possible).
     * @return Current MediaPlayer (null is possible).
     */
    private MediaPlayer getMediaPlayer() {
        return this.mediaPlayer;
    }

    //endregion Getters & Setters

    //region Private methods

    /**
     * Creates a JavaFX Media Object from a Song object.
     * This method is not in the shared code module, because it uses JavaFX.
     * @param song The song object containing the path.
     * @return Returns a Media object with the Song.
     */
    private Media createMediaFromSong(Song song) {

        // Have to create a temporary file to convert the path to a URI.
        File tempFile = new File(song.getPath());

        return new Media(tempFile.toURI().toString());
    }

    /**
     * Initializes a new MediaPlayer according to the Media object passed in the parameters.
     * Be aware that you should dispose unused MediaPlayers!
     * @param media The media used in the MediaPlayer.
     */
    private MediaPlayer initializeMediaPlayer(Media media) {
        MediaPlayer mediaPlayer = new MediaPlayer(media);

        this.currentMediaTimeProperty().bind(mediaPlayer.currentTimeProperty());
        this.totalMediaDurationProperty().bind(mediaPlayer.totalDurationProperty());

        // Volume property.
        mediaPlayer.setVolume(this.getVolume());
        this.volumeProperty().bindBidirectional(mediaPlayer.volumeProperty());

        // Add Listener for the isPlaying property.
        // Maps the different MediaPlayer.Status to the boolean value isPlaying.
        mediaPlayer.statusProperty().addListener((observable, oldValue, newValue) -> {
            if ((oldValue == MediaPlayer.Status.PLAYING) || (newValue == MediaPlayer.Status.PLAYING)) {
                if (newValue == MediaPlayer.Status.PLAYING)
                    this.isPlayingProperty().set(true);
                else
                    this.isPlayingProperty().set(false);
            }
        });

        return mediaPlayer;
    }

    //endregion Private methods

    //region MediaPlayer Interface

    /**
     * Plays a track.
     *
     * @param track The track to play.
     */
    @Override
    public void play(Song track) {
        this.getMediaPlayer(track).play();
    }

    /**
     * Pauses the currently played track.
     * If there is no currently playing track, doesn't do anything.
     */
    @Override
    public void pause() {
        if(this.getMediaPlayer() != null) {
            this.getMediaPlayer().pause();
        }
    }

    /**
     * Stops and disposes the player, regardless of the current status.
     * Can be used always, without checking the status.
     */
    @Override
    public void stop() {

        if(this.getMediaPlayer() != null) {
            this.getMediaPlayer().stop();
        }
    }

    //endregion MediaPlayer Interface
}

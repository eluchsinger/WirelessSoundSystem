package controllers.io.cache.file;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import models.networking.dtos.commands.CacheSongCommand;
import models.networking.dtos.models.CachedSong;
import models.songs.Mp3Song;
import models.songs.Song;
import org.junit.Assert;
import org.junit.Test;
import utils.media.SongUtils;

import java.io.IOException;

/**
 * Created by Esteban on 27.04.2016.
 * Tests the SongCacheManager class.
 */
public class SongCacheManagerTest {

    @Test
    public void store() throws Exception {
        SongCacheManager manager = new SongCacheManager();
        CachedSong cachedSong = this.initCachedSong();
        manager.store(cachedSong);
    }

    @Test
    public void exists() throws Exception {
        SongCacheManager manager = new SongCacheManager();
        CachedSong cachedSong = this.initCachedSong();

        manager.store(cachedSong);

        Assert.assertTrue(manager.exists(cachedSong));
    }

    @Test
    public void retrieve() throws Exception {

        SongCacheManager manager = new SongCacheManager();
        CachedSong cachedSong = this.initCachedSong();

        manager.store(cachedSong);

        Song song = manager.retrieve(cachedSong);

        Assert.assertNotNull(song);
    }

    private CachedSong initCachedSong() throws InvalidDataException, IOException, UnsupportedTagException {

        Song song = new Mp3Song(this.getClass().getResource("/music/AnchorsAweighChorusOnly.mp3").getPath());
        return new CachedSong(SongUtils.getSongData(song));
    }

}
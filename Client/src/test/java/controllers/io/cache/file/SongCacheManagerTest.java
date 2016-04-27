package controllers.io.cache.file;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import models.networking.dtos.CacheSongCommand;
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
        CacheSongCommand cmd = this.initCacheSongCommand();
        manager.store(cmd);
    }

    @Test
    public void exists() throws Exception {
        SongCacheManager manager = new SongCacheManager();
        CacheSongCommand cmd = this.initCacheSongCommand();

        manager.store(cmd);

        Assert.assertTrue(manager.exists(cmd));
    }

    @Test
    public void retrieve() throws Exception {

        SongCacheManager manager = new SongCacheManager();
        CacheSongCommand cmd = this.initCacheSongCommand();

        manager.store(cmd);

        Song song = manager.retrieve(cmd);

        Assert.assertNotNull(song);
    }

    private CacheSongCommand initCacheSongCommand() throws InvalidDataException, IOException, UnsupportedTagException {

        Song song = new Mp3Song(this.getClass().getResource("/music/AnchorsAweighChorusOnly.mp3").getPath());
        return new CacheSongCommand(SongUtils.getSongData(song));
    }

}
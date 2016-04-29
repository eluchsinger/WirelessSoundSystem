package controllers.io.cache.file;

import controllers.io.cache.CacheService;

import java.net.URI;

/**
 * <pre>
 * Created by Esteban Luchsinger on 16.03.2016.
 * The File Cache Service provides an interface to write and read
 * cache data in a file based cache.
 * </pre>
 */
public interface FileCacheService extends CacheService {

    /**
     * @return Returns the absolute path of cache.
     * @throws Exception Throws an exception if the absolute file path could not be found.
     */
    String getAbsoluteFilePath() throws Exception;

    /**
     * @return Returns the URL of the cache.
     */
    URI getFileURI();
}

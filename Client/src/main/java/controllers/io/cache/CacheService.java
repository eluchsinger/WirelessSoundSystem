package controllers.io.cache;

import java.io.IOException;

/**
 * <pre>
 * Created by Esteban Luchsinger on 15.03.2016.
 * The Cache Service provides an interface to cache data into whatever kind of
 * cache is needed.
 * </pre>
 */
public interface CacheService {

    /**
     * Writes the data into the cache.
     * @param data Data to write in the cache.
     * @throws IOException If the cache could not be expanded, an IOException is thrown.
     */
    void writeData(byte[] data) throws IOException;

    /**
     * <pre>
     * Resets the cache.
     * All data currently cached is lost.
     *
     * (Depending on the cache, this might be required to use before closing the application)
     * </pre>
     */
    void reset();
}

package ch.wirelesssoundsystem.shared.models.networking;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Esteban Luchsinger on 18.12.2015.
 * The cache stores the Songs inside a TreeMap.
 * The index of the TreeMap uses the SequenceNr. to sort the packets.
 */
public class SongCache {
    private Map<Integer, SongDatagram> cache;
    private final int expectedSize;

    public SongCache(int expectedSize){
        cache = new TreeMap<>();
        this.expectedSize = expectedSize;
    }

    public void add(SongDatagram... songDatagrams){
        Map<Integer, SongDatagram> temporaryTreeMap = new TreeMap<>();

        for(SongDatagram songDatagram : songDatagrams){
            if(songDatagram.getSequenceNr() == SongDatagram.SEQUENCE_NUMBER_NOT_INITIALIZED)
                throw new IllegalStateException("At least one SongDatagram contains a not-initialized " +
                        "SequenceNr.");
            temporaryTreeMap.put(songDatagram.getSequenceNr(), songDatagram);
        }

        // Put the temporary TreeMap, if no errors were found...
        this.cache.putAll(temporaryTreeMap);
    }

    /**
     * Returns the datagram containing the specified sequenceNr.
     * @param sequenceNr SequenceNr of the datagram searched.
     * @return Returns the datagram containing the specified sequenceNr.
     */
    public SongDatagram getDatagram(int sequenceNr){
        return this.cache.getOrDefault(sequenceNr, null);
    }

    public boolean contains(int sequenceNr){
        return this.cache.containsKey(sequenceNr);
    }

    /**
     * Checks if the cache is completed.
     * Compares the size of the cache with the expectedSize.
     * @return True, if the cache is complete.
     */
    public boolean cacheIsComplete(){
        return this.cache.size() == this.expectedSize;
    }
}

package ch.wirelesssoundsystem.shared.models.networking;

import ch.wirelesssoundsystem.shared.models.networking.exceptions.CacheOverflowException;
import ch.wirelesssoundsystem.shared.models.songs.Song;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * Creates a SongCache.
     * @param expectedSize Expected amount of packets in the cache.
     */
    public SongCache(int expectedSize){
        this.cache = new TreeMap<>();
        this.expectedSize = expectedSize;
    }

    public void add(List<SongDatagram> songDatagrams){
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
    public SongDatagram getSongDatagram(int sequenceNr){
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
    public boolean isComplete(){
        return this.cache.size() == this.expectedSize;
    }

    /**
     *
     * @return Returns the expected size of this cache.
     */
    public int getExpectedSize() {
        return expectedSize;
    }

    /**
     * Gets the missing sequence numbers.
     * @return List of the missing sequence numbers.
     * If there are no missing datagrams, returns an empty List<Integer>.
     */
    public List<Integer> getMissingSequenceNumbers() throws CacheOverflowException {

        int amountOfMissingPackets = this.expectedSize - this.cache.size();

        if(amountOfMissingPackets < 0){
            throw new CacheOverflowException("There are more datagrams in the cache than expected.");
        }

        // Create arrayList with the initialCapacity of the missing packets.
        List<Integer> missingSequences = new ArrayList<>(amountOfMissingPackets);

        // If the cache is already complete, there is nothing to do.
        if(!this.isComplete()){
            for(int i = 0; i < this.cache.size(); i++) {

                // Datagram missing
                if(this.cache.get(i) == null){
                    missingSequences.add(i);
                }

                // Check if there are are more sequences missing (just the amount)
                if(missingSequences.size() >= amountOfMissingPackets){
                    break;
                }
            }
        }

        return missingSequences;
    }

    /**
     * Creates a SongCache for the song.
     * @param song
     * @return
     */
    public static SongCache cacheSong(Song song) throws IOException {
        // Get File Data.
        File songFile = new File(song.getPath());
        byte[] fileData = Files.readAllBytes(songFile.toPath());
        List<SongDatagram> datagrams = SongDatagram.createPackets(fileData);
        SongCache cache = new SongCache(datagrams.size());
        cache.add(datagrams);

        return cache;
    }
}
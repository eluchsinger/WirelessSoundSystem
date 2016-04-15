package models.networking;


import models.songs.Song;
import utils.networking.SongDatagramBuilder;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <pre>
 * Created by Esteban Luchsinger on 18.12.2015.
 * The cache stores the Songs inside a TreeSet.
 * </pre>
 */
public class SongCache {
    private Set<SongDatagram> cache;
    private final int expectedCacheSize;

    /**
     * Creates a SongCache.
     * @param expectedCacheSize Expected amount of packets in the cache.
     */
    public SongCache(int expectedCacheSize){
        this.cache = new TreeSet<>();
        this.expectedCacheSize = expectedCacheSize;
    }

    public void add(SongDatagram songDatagram){
        this.cache.add(songDatagram);
    }

    public void add(List<SongDatagram> songDatagrams){
        this.cache.addAll(songDatagrams);
    }

    /**
     * Returns the datagram containing the specified sequenceNr.
     * @param sequenceNr SequenceNr of the datagram searched.
     * @return Returns the datagram containing the specified sequenceNr.
     */
    public SongDatagram getSongDatagram(int sequenceNr) {
        Optional<SongDatagram> optional = this.cache.stream().filter(sd -> sd.getSequenceNumber() == sequenceNr).findFirst();
        return optional.orElse(null);
    }

    public boolean contains(int sequenceNr){
        return this.cache.stream().anyMatch(sd -> sd.getSequenceNumber() == sequenceNr);
    }

    /**
     * Checks if the cache is completed.
     * Compares the size of the cache with the expectedCacheSize.
     * @return True, if the cache is complete.
     */
    public boolean isComplete(){
        return (this.cache.size() == this.expectedCacheSize);
    }

    /**
     *
     * @return Returns the expected size of this cache.
     */
    public int getExpectedCacheSize() {
        return expectedCacheSize;
    }

    /**
     * Gets the missing sequence numbers.
     * @return List of the missing sequence numbers. If there are no missing numbers,
     * returns an empty list.
     * If there are no missing datagrams, returns an empty List&lt;Integer&gt;.
     */
    public List<Integer> getMissingSequenceNumbers() throws Exception /*throws CacheOverflowException */{

        if(this.expectedCacheSize <= 0)
            throw new Exception("The expected size is zero or smaller!");
        if(this.cache.size() > this.expectedCacheSize)
            throw new Exception("The Cache is bigger than expected!");

        // Return empty list.
        if(this.cache.size() == this.expectedCacheSize) {

            return new ArrayList<Integer>();
        }
        else {
            int amountOfMissingPackets = this.expectedCacheSize - this.cache.size();

            // Creates a list starting from 1 until amountOfMissingPackets.
            List<Integer> possibilities = IntStream
                    .iterate(1, i -> i + 1)
                    .limit(amountOfMissingPackets)
                    .boxed()
                    .collect(Collectors.toList());

            // Creates a list of the existing Integers.
            List<Integer> existing = this.cache.stream()
                    .mapToInt(sd -> sd.getSequenceNumber())
                    .boxed()
                    .collect(Collectors.toList());

            // Remove all existing sequences from the possibilities.
            possibilities.removeAll(existing);

            return possibilities;
        }
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
        List<SongDatagram> datagrams = SongDatagramBuilder.createPackets(fileData);
        SongCache cache = new SongCache(datagrams.size());
        cache.add(datagrams);

        return cache;
    }

    public static SongCache cacheSong(Song song, InetAddress inetAddress, int port) throws IOException {
        // Get File Data.
        File songFile = new File(song.getPath());
        byte[] fileData = Files.readAllBytes(songFile.toPath());
        List<SongDatagram> datagrams = SongDatagramBuilder.createPackets(fileData, inetAddress, port);
        SongCache cache = new SongCache(datagrams.size());
        cache.add(datagrams);

        return cache;
    }
}
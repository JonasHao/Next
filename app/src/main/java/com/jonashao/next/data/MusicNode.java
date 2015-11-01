package com.jonashao.next.data;

import com.jonashao.next.constants.Config;

/**
 * Created by Junnan on 2015/10/29.
 * A node in the chain of appetite
 */
public class MusicNode {

    private long ID;
    private long LastID;
    private long NextID;
    private long[] Disliked;
    private long Dislike;

    MusicNode(long ID) {
        this.ID = ID;
    }

    MusicNode(long ID, long LastID){
        this.ID = ID;
        this.LastID = LastID;
    }

    static long[] justPlayed = new long[Config.PLAYED_MEM_SIZE];

    /**
     * choose a song in its disliked list, so the style of music might be changed.
     *
     * @return the chosen song's ID
     */
    public long changeAppetite() {

        return 0;
    }

    /**
     * choose a song similar with the one just played(on the "like chain")
     *
     * @return the chosen song's ID
     */
    public long oneMore() {
        return 0;
    }

    /**
     * choose a song randomly in source songs.
     *
     * @param source the source of songs waiting to be chosen.
     * @return the selected song's ID
     */
    private long randomChoose(long[] source) {

        return 0;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public long getLastID() {
        return LastID;
    }

    public void setLastID(long lastID) {
        LastID = lastID;
    }

    public long getNextID() {
        return NextID;
    }

    public void setNextID(long nextID) {
        NextID = nextID;
    }

    public long[] getDisliked() {
        return Disliked;
    }

    public void setDisliked(long[] disliked) {
        Disliked = disliked;
    }

    public long getDislike() {
        return Dislike;
    }

    public void setDislike(long dislike) {
        Dislike = dislike;
    }
}

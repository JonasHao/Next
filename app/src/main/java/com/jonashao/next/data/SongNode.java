package com.jonashao.next.data;

import com.jonashao.next.constants.Config;

/**
 * Created by Junnan on 2015/10/29.
 * A node in the chain of appetite
 */
public class SongNode {
    long ID;
    long LastID;
    long NextID;
    long[] Disliked;
    long Dislike;

    static long[] justPlayed = new long[Config.PLAYED_MEM_SIZE];

    /**
     * choose a song in its disliked list, so the style of music might be changed.
     * @return the chosen song's ID
     */
    public long changeAppetite(){

        return 0;
    }

    /**
     * choose a song similar with the one just played(on the "like chain")
     * @return the chosen song's ID
     */
    public long oneMore(){
        return 0;
    }

    /**
     * choose a song randomly in source songs.
     * @param source the source of songs waiting to be chosen.
     * @return the selected song's ID
     */
    private long randomChoose(long[] source){

        return 0;
    }


}

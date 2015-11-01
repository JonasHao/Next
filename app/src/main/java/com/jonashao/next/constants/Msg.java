package com.jonashao.next.constants;

/**
 * Created by Junnan on 2015/10/28.
 *
 */
public class Msg {
    // what in MusicService
    public final static int MSG_REGISTER = 0;
    public final static int MSG_ACTION = 1;
    // what in MainActivity
    public final static int MSG_MUSIC_INFO = 0;

    // arg
    public final static int ACTIVITY_MAIN = 0;
    // args of MSG_ACTION
    public final static int ACTION_PLAY = 0;
    public final static int ACTION_PAUSE = 1;
    public final static int ACTION_PREVIOUS = 2;
    public final static int ACTION_LIKE = 3;
    public final static int ACTION_NEXT = 4;

    // Bundle keys of Music_Info
    public final static String ID = "id";
    public final static String TITLE="title";
    public final static String ARTIST="artist";
    public final static String COVER_URL="cover_url";


}

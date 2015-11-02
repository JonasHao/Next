package com.jonashao.next.data;

/**
 * Created by Junnan on 2015/10/28.
 */
public class MusicFeatures {

    private MusicInfo mMusicInfo;
    private String album;
    private int year;

    private String genre;

    public static long generatID() {
        // TODO: auto generate
        return 0;
    }

    public MusicFeatures(long id, String title, String artist, int duration){
        this.mMusicInfo =  new MusicInfo(id, title, artist);
        mMusicInfo.setDuration(duration);
    }

    public MusicFeatures(long id, String title, String artist) {
        this.mMusicInfo = new MusicInfo(id, title, artist);
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public MusicInfo getmMusicInfo() {
        return mMusicInfo;
    }

    public void setmMusicInfo(MusicInfo mMusicInfo) {
        this.mMusicInfo = mMusicInfo;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}

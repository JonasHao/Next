package com.jonashao.next.data;

/**
 * Created by Junnan on 2015/10/29.
 */
public class MusicInfo {


    private long ID;
    private String title;
    private String artist;
    private String coverUrl;
    private int duration;

    MusicInfo(long ID, String title, String artist) {
        setID(ID);
        setTitle(title);
        setArtist(artist);
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}

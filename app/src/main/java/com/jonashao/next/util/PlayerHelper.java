package com.jonashao.next.util;

import android.content.ContentUris;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;
import android.widget.Toast;

import com.jonashao.next.data.DBOpenHelper;

import java.io.IOException;

/**
 * Created by Junnan on 2015/10/28.
 * Helper class for playing music
 */
public class PlayerHelper implements AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    public enum State{IDLE, START, PAUSE, STOP}
    State mState;

    private static final String TAG = "PlayerHelper";
    MediaPlayer mMediaPlayer;
    DBOpenHelper musicDB;
    Context context;


    public PlayerHelper(Context context) {
        this.context = context;
        initMediaPlayer();
        musicDB = DBOpenHelper.getInstance(context);
        mState = State.IDLE;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        switchState(State.START);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        LogHelper.e(TAG, "error when prepare play");
        switchState(State.STOP);
        release();
        initMediaPlayer();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        switchState(State.STOP);
        LogHelper.i("Completion Listener", "Song Complete");
        mp.stop();
        mp.reset();
        try {
            mp.setDataSource(context, musicDB.completionNext());
        } catch (IOException e) {
            LogHelper.e(TAG, "SQL failed");
        }
        mp.prepareAsync();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mMediaPlayer == null) initMediaPlayer();
                else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
                mMediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private void initMediaPlayer() {
        release();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // prevent the CPU from sleeping
        mMediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setOnPreparedListener(this);

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Toast.makeText(context, "Could not get audio focus", Toast.LENGTH_SHORT).show();
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            switchState(State.STOP);
        }
    }

    public long findLastPlayed() {
        return musicDB.findLastPlayed();
    }

    public boolean play() {
        switch (mState){
            case IDLE:
                return false;
            case START:
            case PAUSE:
                mMediaPlayer.start();
                switchState(State.START);
                return true;
            default:
                return false;
        }
    }

    public void play(long id) {
        try {
            initMediaPlayer();
            Uri contentUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
            try {
                mMediaPlayer.setDataSource(context, contentUri);
            } catch (IOException e) {
                LogHelper.e(TAG, "无法根据id" + id + "找到歌曲", e);
            }
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            LogHelper.e(TAG, e);
        }
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void previous() {
        long id = findLastPlayed();
        play(id);
    }

    public void like() {
        Toast.makeText(context,"还没做好o",Toast.LENGTH_SHORT).show();
    }

    public void next() {
        Toast.makeText(context,"还没做好o",Toast.LENGTH_SHORT).show();
    }

    private boolean switchState(State state){
        switch (state){
            case IDLE:
                if(mState == State.STOP) {
                    mState = State.IDLE;
                    return true;
                }
                break;
            case START:
                switch (mState){
                    case IDLE:
                    case START:
                    case PAUSE:
                        mState = State.START;
                        return true;
                }
                break;
            case PAUSE:
                if(mState == State.START) {
                    mState = State.START;
                    return true;
                }
                break;
            case STOP:
                switch (mState){
                    case IDLE:
                        break;
                    case START:
                    case PAUSE:
                        mState = State.STOP;
                        return true;
                }
                break;
        }
        return false;
    }

    public final State getState(){
        return mState;
    }

    public boolean justPlayed(long id){
        return false;
    }

}

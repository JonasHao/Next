package com.jonashao.musicrecommender;

import android.content.ContentUris;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;

import android.widget.Toast;

import com.jonashao.musicrecommender.models.Music;
import com.jonashao.musicrecommender.util.LogHelper;


import java.io.IOException;


/**
 * Created by Junnan on 2015/10/28.
 * Helper class for playing music
 */
public class PlayerHelper implements AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private static final String TAG = PlayerHelper.class.getSimpleName();

    private MediaPlayer mMediaPlayer = null;


    private Music now = null;
    private boolean inPlayQueue;
    private boolean inPreparing;

    private Context context;
    private MediaPlayer.OnCompletionListener mCompletionListener;

    public PlayerHelper(Context context, MediaPlayer.OnCompletionListener listener) {
        this.context = context;
        mCompletionListener = listener;
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        LogHelper.i(TAG, "onPrepared");
        LogHelper.d(TAG, "In play queue? " + inPlayQueue);
        inPreparing = false;
        if (inPlayQueue) {
            try {
                inPlayQueue = false;
                play();
            } catch (IllegalStateException e) {
                LogHelper.e(TAG, "call play() in illegal state", e);
            }
        }
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        LogHelper.e(TAG, "error when prepare play");
        release();
        return false;
    }

    @SuppressWarnings("unused")
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    @SuppressWarnings("unused")
    public int getPos() {
        return mMediaPlayer == null ? -1 : mMediaPlayer.getCurrentPosition();
    }

    @SuppressWarnings("unused")
    public Music getNow() {
        return now;
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
        mMediaPlayer.setOnCompletionListener(mCompletionListener);
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void play() throws IllegalStateException {
        if (mMediaPlayer != null) {
            if (inPreparing) {
                LogHelper.d(TAG, "In preparing, set inPlayQueue flag");
                inPlayQueue = true;
                return;
            }
            mMediaPlayer.start();
        }
    }

    public void pause() throws IllegalStateException {
        mMediaPlayer.pause();
    }


    public void prepareSource(Music node) {
        try {
            initMediaPlayer();
            setPlaySourceID(mMediaPlayer, node);
            mMediaPlayer.prepareAsync();
            inPreparing = true;
        } catch (Exception e) {
            LogHelper.e(TAG, e);
        }
    }

    private void setPlaySourceID(MediaPlayer mp, Music node) {
        now = node;
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, now.getRID());
        try {
            mp.setDataSource(context, contentUri);

        } catch (IOException e) {
            LogHelper.e(TAG, "无法根据id " + node.getId() + "找到歌曲" + node.getTitle(), e);
        }
    }

}

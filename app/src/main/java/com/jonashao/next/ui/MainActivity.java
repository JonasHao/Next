package com.jonashao.next.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jonashao.next.MusicService;
import com.jonashao.next.R;
import com.jonashao.next.constants.Msg;
import com.jonashao.next.util.LogHelper;

import java.lang.ref.WeakReference;

import co.mobiwise.library.MusicPlayerView;

public class MainActivity extends MusicBaseActivity implements View.OnClickListener {
    private static final String TAG ="MainActivity";
    //Views
    MusicPlayerView mpv;
    ImageButton button_previous;
    ImageButton button_like;
    ImageButton button_next;
    TextView textView_title;
    TextView textView_artist;
    RelativeLayout panel_back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();

        Intent intent = new Intent(MainActivity.this, MusicService.class);
        startService(intent);
        LogHelper.d(TAG, "service started");
        BindService();
    }

    private void findViews() {
        mpv = (MusicPlayerView) findViewById(R.id.mpv);
        button_previous = (ImageButton) findViewById(R.id.previous);
        button_like = (ImageButton) findViewById(R.id.like);
        button_next = (ImageButton) findViewById(R.id.next);
        textView_title = (TextView)findViewById(R.id.textView_title);
        textView_artist =(TextView)findViewById(R.id.textView_artist);
        panel_back = (RelativeLayout)findViewById(R.id.background_panel);

        mpv.setOnClickListener(this);
        button_previous.setOnClickListener(this);
        button_like.setOnClickListener(this);
        button_next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mpv:
                if (mpv.isRotating()) {
                    sendToService(Msg.MSG_ACTION, Msg.ACTION_PAUSE);
                    mpv.stop();
                } else {
                    sendToService(Msg.MSG_ACTION, Msg.ACTION_PLAY);
                    mpv.start();
                }
                break;
            case R.id.previous:
                sendToService(Msg.MSG_ACTION, Msg.ACTION_PREVIOUS);
                break;
            case R.id.like:
                sendToService(Msg.MSG_ACTION, Msg.ACTION_LIKE);
                break;
            case R.id.next:
                sendToService(Msg.MSG_ACTION, Msg.ACTION_NEXT);
                break;

        }
    }

    @Override
         Handler incomingHandler() {
        return new mainActivityHandler(this);
    }

    @Override
    int getActivityCode() {
        return Msg.ACTIVITY_MAIN;
    }

    private static class mainActivityHandler extends Handler {
        private final WeakReference<MainActivity> mWeakReference;
        mainActivityHandler(MainActivity mainActivity) {
            mWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mWeakReference.get();
            LogHelper.d(TAG,"receive: "+msg.what);
            switch (msg.what) {
                case Msg.MSG_MUSIC_INFO:
                    Bundle musicInfo = msg.getData();
                    String title = musicInfo.getString(Msg.TITLE);
                    String artist = musicInfo.getString(Msg.ARTIST);
                    String cover_url = musicInfo.getString(Msg.COVER_URL);
                    int duration = musicInfo.getInt(Msg.DURATION);
                    LogHelper.d(TAG,"duration "+ duration);
                    activity.setMusicDisplay(title,artist,cover_url);
                    activity.mpv.setMax(duration/1000);
                    break;

                default:
                    super.handleMessage(msg);
            }


        }
    }

    private void setMusicDisplay(String title, String artist, String CoverUrl){
        textView_title.setText(title);
        textView_artist.setText(artist);
        mpv.setCoverURL(CoverUrl);

//      Todo: generate palette color from cover
    }
}


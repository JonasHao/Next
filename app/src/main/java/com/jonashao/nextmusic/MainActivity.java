package com.jonashao.nextmusic;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jonashao.musicplayerview.MusicPlayerView;
import com.jonashao.musicplayerview.OnColorListener;
import com.jonashao.musicplayerview.OnTouchEventListener;
import com.jonashao.musicrecommender.MusicActionListener;
import com.jonashao.musicrecommender.MusicView;
import com.jonashao.musicrecommender.models.Music;
import com.jonashao.musicrecommender.util.Constants;
import com.jonashao.musicrecommender.util.DisplayCallback;
import com.jonashao.musicrecommender.util.LogHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements MusicActionListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    MusicView musicView;
    @BindView(R.id.title)TextView textView_title;
    @BindView(R.id.artist) TextView textView_artist;
    @BindView(R.id.mpv) MusicPlayerView mpv;
    @BindView(R.id.panel) RelativeLayout panel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        musicView = new MusicView(this, callback);
        musicView.setNoticeActivity(MainActivity.class);
        checkPermission();

        mpv.setTouchEventListener(mTouchEventListener);
        mpv.setColorListener(mColorListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        musicView.BindService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        musicView.UnbindService();
    }



    public void checkPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                Constants.PERMISSION_WRITE_EXTERNAL_STORAGE);

        musicView.noticePermission(Constants.PERMISSION_WRITE_EXTERNAL_STORAGE, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.PERMISSION_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    musicView.noticePermission(Constants.PERMISSION_WRITE_EXTERNAL_STORAGE, true);
                }
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    DisplayCallback callback = new DisplayCallback() {
        @Override
        public void display(Music music) {
            LogHelper.d(TAG, music.toString() + "\talbumID:" + music.getAlbumID() + " \n" + music.getCoverPath());
            textView_title.setText(music.getTitle());
            textView_artist.setText(music.getArtist());
            mpv.setMax(music.getDuration());
            mpv.setCoverUrl(music.getAlbumID());
        }

        @Override
        public void refreshProgress(int progress) {
            mpv.setProgress(progress);
        }

        @Override
        public void feedback(int action_code) {
            switch (action_code) {
                case MusicView.PLAY:
                    mpv.start();
                    break;
            }
        }
    };

    OnTouchEventListener mTouchEventListener = new OnTouchEventListener() {
        @Override
        public void onDoubleClicked() {
            if (mpv.isPlaying()) {
                mpv.stop();
                musicView.pause();
            } else {
                mpv.start();
                musicView.play();
            }
        }

        @Override
        public void onFling() {
            musicView.next();
        }
    };

    OnColorListener mColorListener = new OnColorListener() {
        @Override
        public void applyColor(int color) {
            panel.setBackgroundColor(color);
        }
    };

    @Override
    public void pause() {
        mpv.stop();
    }
}

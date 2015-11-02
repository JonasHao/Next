package com.jonashao.next;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import com.jonashao.next.constants.Msg;
import com.jonashao.next.data.DBOpenHelper;
import com.jonashao.next.data.MusicInfo;
import com.jonashao.next.data.MusicProvider;
import com.jonashao.next.util.LogHelper;
import com.jonashao.next.util.PlayerHelper;
//import com.jonashao.next.util.PlayerHelper;

import java.lang.ref.WeakReference;

public class MusicService extends Service {
    private static final int NOTIFICATION_ID = 109;
    private static final String TAG = "Service";

    Messenger replyTo;
    PlayerHelper mPlayerHelper;
    MusicProvider musicProvider;

    public MusicService() {
        //todo: Set MusicService to be a foreground service
//        String songName;
//        Intent onClickNotificationIntent = new Intent(getApplicationContext(), MainActivity.class);
//        // assign the song name to songName
//        PendingIntent pi = PendingIntent.getActivity
//                (getApplicationContext(), 0, onClickNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        Notification notification = new Notification();
//        //todo: 定义NOTIFICATION
//        startForeground(NOTIFICATION_ID, notification);

//        musicProvider = MusicProvider.getInstance(getApplicationContext());
//        musicProvider.scanMusic();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            mPlayerHelper = new PlayerHelper(this);
        } catch (NullPointerException e) {
            LogHelper.e(TAG, "Null pointer");
        }
    }

    /*When clients binding to the service, we return an interface to our messenger
        for sending messages to the service.*/
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }


    //send message to the IncomingHandler
    final Messenger mMessenger = new Messenger(new IncomingHandler(this));


    private static class IncomingHandler extends Handler {
        private final WeakReference<MusicService> mWeakReference;

        private IncomingHandler(MusicService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mWeakReference.get();
            LogHelper.d(TAG, "RECEIVE MSG:" + msg.what);
            switch (msg.what) {
                case Msg.MSG_REGISTER:
                    service.replyTo = msg.replyTo;
                    break;
                case Msg.MSG_ACTION:
                    switch (msg.arg1) {
                        case Msg.ACTION_PLAY:
                            if (! service.mPlayerHelper.play()) {
                                // the previous state of mediaPlayer was not PAUSE
                                long id = service.mPlayerHelper.findLastPlayed();
                                DBOpenHelper musicDB = DBOpenHelper.getInstance(service.getApplicationContext());
                                LogHelper.d(TAG,"To query with id:"+id);
                                MusicInfo musicInfo = musicDB.queryMusicInfoByID(id);
                                if(musicInfo == null){
                                    Toast.makeText(service, service.getString(R.string.query_music_failed),Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                Message message = Message.obtain(null, Msg.MSG_MUSIC_INFO);
                                Bundle inf = new Bundle();
                                inf.putString(Msg.TITLE, musicInfo.getTitle());
                                inf.putString(Msg.ARTIST, musicInfo.getArtist());
                                inf.putLong(Msg.ID, musicInfo.getID());
                                inf.putInt(Msg.DURATION, musicInfo.getDuration());
                                message.setData(inf);
                                service.sendToTarget(message);
                                if (id > 0) {
                                    service.mPlayerHelper.play(id);
                                } else {
                                    LogHelper.e(TAG, "没有找到可以播放的歌曲");
                                }
                            }
                            break;
                        case Msg.ACTION_PAUSE:
                            service.mPlayerHelper.pause();
                            break;
                        case Msg.ACTION_PREVIOUS:
                            service.mPlayerHelper.previous();
                            break;
                        case Msg.ACTION_LIKE:
                            service.mPlayerHelper.like();
                            break;
                        case Msg.ACTION_NEXT:
                            service.mPlayerHelper.next();
                            break;
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayerHelper != null) {
            mPlayerHelper.release();
        }
    }

    private void sendToTarget(int what) {
        Message msg = Message.obtain(null, what);
        sendToTarget(msg);
    }

    private void sendToTarget(Message msg) {
        try {
            replyTo.send(msg);
        } catch (RemoteException e) {
            LogHelper.e(TAG, "无法向Activity发送消息", e);
        }
    }

//    private static class WorkingHandler extends Handler{
//        private final WeakReference<MusicService> mWeakReference;
//
//        private WorkingHandler(MusicService service) {
//            mWeakReference = new WeakReference<>(service);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            MusicService service = mWeakReference.get();
//            super.handleMessage(msg);
//            Message message = Message.obtain(null,msg.what,msg.arg1,msg.arg2);
//            message.setData(msg.getData());
//            service.sendToTarget(msg);
//        }
//    }

}

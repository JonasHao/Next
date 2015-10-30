package com.jonashao.next;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.jonashao.next.constants.Msg;
import com.jonashao.next.ui.MainActivity;
import com.jonashao.next.util.PlayerHelper;

import java.lang.ref.WeakReference;

public class MusicService extends Service {
    private static final int NOTIFICATION_ID = 109;

    Messenger replyTo;
    PlayerHelper mPlayerHelper;

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
            switch (msg.what) {
                case Msg.MSG_REGISTER:
                    service.replyTo = msg.replyTo;
                    break;
                case Msg.MSG_ACTION:
                    switch (msg.arg1){
                        case Msg.ACTION_PLAY:
                            service.mPlayerHelper.play();
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
        if(mPlayerHelper != null){
            mPlayerHelper.release();
        }
    }
}

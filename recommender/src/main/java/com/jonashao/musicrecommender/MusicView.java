package com.jonashao.musicrecommender;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.jonashao.musicrecommender.models.DaoMaster;
import com.jonashao.musicrecommender.models.Music;
import com.jonashao.musicrecommender.util.Constants;
import com.jonashao.musicrecommender.util.DisplayCallback;
import com.jonashao.musicrecommender.util.LogHelper;

import java.lang.ref.WeakReference;


/**
 * Created by Koche on 2016/3/10.
 * <p>View part of the MVC framework.</p>
 * <p>
 * MusicView bind with a user interface, which gets input from user and display info to user,
 * and those input and display information flow in and out the framework via MusicView.
 * </p>
 */
public class MusicView {
    private static final String TAG = MusicView.class.getSimpleName();

    private Messenger mServiceMessenger = null;
    private boolean mBounded = false;

    private Context mContext = null;
    private DisplayCallback mCallback = null;

    private DaoMaster daoMaster = null;
    private MusicActionListener musicActionListener = null;
    private MusicIntentReceiver musicIntentReceiver = null;
    private ProgressDialog dialog = null;

    public static final int PLAY = 0;
    public static final int PAUSE = 1;
    public static final int NEXT = 2;
    public static final int LOVE = 3;
    public static final int COMPLETE = 4;

    private Class notice;


    public MusicView(Context context, DisplayCallback callback) {
        mContext = context;
        mCallback = callback;

        musicActionListener = (MusicActionListener) context;
        musicIntentReceiver = new MusicIntentReceiver();

        dialog = new ProgressDialog(context);

        Intent intent = new Intent(mContext, MusicService.class);
        mContext.startService(intent);

        BindService();

        LogHelper.d(TAG, "service started");
    }

    final Messenger mMessenger = new Messenger(new Handler(this));

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceMessenger = new Messenger(service);
            mBounded = true;

            Message msg = Message.obtain(null, MusicService.MSG_REGISTER);
            msg.replyTo = mMessenger;
            sendToService(msg);

            if (notice != null) {
                msg = Message.obtain(null, MusicService.MSG_NOTICE);
                Bundle data = new Bundle();
                data.putSerializable(Constants.KEY_NOTICE, notice);
                msg.setData(data);
                sendToService(msg);
                notice = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceMessenger = null;
            mBounded = false;
        }
    };


    public void BindService() {
        if (!mBounded) {
            try {
                mContext.registerReceiver(musicIntentReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
                DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(mContext, "music_db", null);
                daoMaster = new DaoMaster(helper.getWritableDatabase());

                mContext.getApplicationContext().
                        bindService(new Intent(mContext, MusicService.class),
                                mConnection, Context.BIND_AUTO_CREATE);
            } catch (Exception e) {
                LogHelper.e(TAG, "Bind Service Failed", e);
            }
        }
    }


    public void UnbindService() {
        if (mBounded) {
            sendToService(MusicService.MSG_REGISTER, 1);
            mContext.getApplicationContext().unbindService(mConnection);

            if (musicIntentReceiver != null) {
                mContext.unregisterReceiver(musicIntentReceiver);
                musicIntentReceiver = null;
            }

            daoMaster = null;
            mBounded = false;
        }
    }

    public void sendToService(int what, int arg1) {
        Message msg = Message.obtain(null, what);
        msg.arg1 = arg1;
        sendToService(msg);
    }


    public void sendToService(int what, int arg1, int arg2) {
        Message msg = Message.obtain(null, what);
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        sendToService(msg);
    }

    public void sendToService(Message msg) {
        for (int i = 0; i < Constants.REPEAT_TRANSMITTING_COUNT; i++) {
            if (mBounded && mServiceMessenger != null) {
                try {
                    mServiceMessenger.send(msg);
                    return;
                } catch (RemoteException e) {
                    LogHelper.e(TAG, "第" + i + "次发送失败", e);
                    BindService();
                }
            } else {
                Log.e(TAG, "第" + i + "次发送失败，mBTService is null");
            }
        }
    }


    public void play() {
        sendToService(MusicService.MSG_ACTION, PLAY);
    }

    public void pause() {
        LogHelper.d(TAG, "pause");
        sendToService(MusicService.MSG_ACTION, PAUSE);
        if (musicActionListener != null) {
            musicActionListener.pause();
        }
    }

    public void next() {
        sendToService(MusicService.MSG_ACTION, NEXT);
    }

    public void love() {
        sendToService(MusicService.MSG_ACTION, LOVE);
    }

    public void setNoticeActivity(Class c) {
        notice = c;
        if (mBounded) {
            Message msg = Message.obtain(null, MusicService.MSG_NOTICE);
            Bundle data = new Bundle();
            data.putSerializable(Constants.KEY_NOTICE, c);
            msg.setData(data);
            sendToService(msg);
            notice = null;
        }
    }


    public void noticePermission(int permissionCode, boolean grant) {
        sendToService(MusicService.MSG_PERMISSION, permissionCode, grant ? 1 : 0);
    }

    private static class Handler extends android.os.Handler {
        private final WeakReference<MusicView> mWeakReference;

        Handler(MusicView musicView) {
            mWeakReference = new WeakReference<>(musicView);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicView musicView = mWeakReference.get();
            switch (msg.what) {
                case MusicService.MSG_DISPLAY:
                    long musicID = ((long) msg.arg2 << 32) | (long) msg.arg1 & 0xFFFFFFFFL;
                    Music music = musicView.daoMaster.newSession().getMusicDao().load(musicID);
                    musicView.mCallback.display(music);
                    break;
                case MusicService.MSG_PROGRESS:
                    musicView.mCallback.refreshProgress(msg.arg1);
                    break;
                case MusicService.MSG_FEEDBACK:
                    musicView.mCallback.feedback(msg.arg1);
                    break;
                case MusicService.MSG_SCAN:
                    musicView.showScanDialog(msg.arg1, msg.arg2);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void showScanDialog(int progress, int max) {
        if (dialog == null) {
            dialog = new ProgressDialog(mContext);
        }

        dialog.setTitle("正在扫描音乐");
        dialog.show();
        dialog.setMax(max);
        dialog.setProgress(progress);
    }

    public class MusicIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                LogHelper.d(TAG, "headset plug");
                int state = intent.getIntExtra("state", -1);
                if (state == 0) {
                    pause();
                }
            }
        }
    }


}

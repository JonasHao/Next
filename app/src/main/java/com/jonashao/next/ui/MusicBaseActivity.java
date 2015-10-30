package com.jonashao.next.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.jonashao.next.MusicService;
import com.jonashao.next.constants.Msg;
import com.jonashao.next.util.LogHelper;


/**
 * Created by Junnan on 2015/10/28.
 * This is the base class for those activities which need to bound with the {@link MusicService}
 */
public abstract class MusicBaseActivity extends AppCompatActivity {
    private static final String TAG = "MusicBase";
    Messenger mServiceMessenger;
    boolean mBounded;

    abstract Handler incomingHandler();

    abstract int getActivityCode();

    final Messenger mMessenger = new Messenger(incomingHandler());

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceMessenger = new Messenger(service);
            mBounded = true;
            try {
                Message msg = Message.obtain(null, Msg.MSG_REGISTER);
                msg.replyTo = mServiceMessenger;
                msg.arg1 = getActivityCode();
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                LogHelper.e(TAG, e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceMessenger = null;
            mBounded = false;
        }
    };


    public void BindService() {
        try {
            bindService(new Intent(this, MusicService.class), mConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            LogHelper.e(TAG, "Bind Service Failed", e);
        }
    }

    public void sendToService(int what, int arg1){
        Message msg = Message.obtain(null,what);
        msg.arg1 = arg1;
        sendToService(msg);
    }

    public void sendToService(int what, int arg1, int arg2){
        Message msg = Message.obtain(null,what);
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        sendToService(msg);
    }

    public void sendToService(Message msg) {
        if (mBounded && mServiceMessenger != null) {
            try {
                mServiceMessenger.send(msg);
                LogHelper.v(TAG, getActivityCode() + " sent " + msg.what);
            } catch (RemoteException e) {
                LogHelper.e(TAG, e);
                BindService();
            }
        } else {
            BindService();
            Log.e(TAG, "mBTService is null");
        }
    }

}

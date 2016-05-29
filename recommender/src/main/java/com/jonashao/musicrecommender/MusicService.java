package com.jonashao.musicrecommender;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.jonashao.musicrecommender.models.DaoMaster;
import com.jonashao.musicrecommender.models.DaoSession;
import com.jonashao.musicrecommender.models.Music;
import com.jonashao.musicrecommender.models.MusicDao;
import com.jonashao.musicrecommender.models.Recorder;
import com.jonashao.musicrecommender.models.RecorderDao;
import com.jonashao.musicrecommender.util.Constants;
import com.jonashao.musicrecommender.util.DisplayCallback;
import com.jonashao.musicrecommender.util.LogHelper;

import java.lang.ref.WeakReference;
import java.util.List;

public class MusicService extends Service implements
        MediaPlayer.OnCompletionListener, MusicFramework.OnErrorListener {

    private static final String TAG = MusicService.class.getSimpleName();

    public static final int MSG_REGISTER = 0;
    public static final int MSG_ACTION = 1;
    public static final int MSG_DISPLAY = 2;
    public static final int MSG_PROGRESS = 3;
    public static final int MSG_PERMISSION = 4;
    public static final int MSG_FEEDBACK = 5;
    public static final int MSG_SCAN = 6;
    public static final int MSG_NOTICE = 7;

    public static final int noticeID = 525;

    LocationManager locationManager;
    MusicIntentReceiver mReceiver;
    NotificationCompat.Builder mBuilder;

    DaoMaster daoMaster;
    MusicFramework framework;

    Messenger replyTo;


    public MusicService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        mReceiver = new MusicIntentReceiver();

        PlayerHelper playerHelper = new PlayerHelper(this, this);
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "music_db", null);
        daoMaster = new DaoMaster(helper.getWritableDatabase());

        checkMusicBase(daoMaster, false);

        framework = MusicFramework.getInstance();
        framework.setPlayer(playerHelper);
        framework.setDaoMaster(daoMaster);
        framework.setErrorListener(this);
    }

    public void checkMusicBase(DaoMaster daoMaster, boolean has_permission) {
        DaoSession session = daoMaster.newSession();
        MusicDao musicDao = session.getMusicDao();
        long count = musicDao.count();

        if (count == 0) {
            if (has_permission || ContextCompat.
                    checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {

                sendToTarget(MSG_SCAN, 0, 100);
                ScanTask scanTask = new ScanTask(this, session);
                scanTask.scanMusic();
                sendToTarget(MSG_SCAN, 100, 100);

                // load history
                RecorderDao recorderDao = session.getRecorderDao();
                List<Recorder> recorders = recorderDao.queryBuilder().limit(10).list();
                for (Recorder recorder : recorders) {
                    recorder.writeHistory();
                }
                return;
            }

            Toast.makeText(this, R.string.no_permission, Toast.LENGTH_SHORT).show();
        }
    }


    /*When clients binding to the service, we return an interface to our messenger
            for sending messages to the service.*/
    @Override
    public IBinder onBind(Intent intent) {
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
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
            WeakReference<MusicFramework> reference = new WeakReference<>(MusicFramework.getInstance());
            MusicFramework framework = reference.get();

            switch (msg.what) {
                case MSG_REGISTER: {
                    if (msg.arg1 == 1) {
                        service.replyTo = null;
                    } else {
                        service.replyTo = msg.replyTo;
                    }
                    framework.setDisplayCallback(service.displayCallback);
                    break;
                }
                case MSG_ACTION:
                    framework.handleAction(msg.arg1);
                    break;
                case MSG_PERMISSION:
                    if (msg.arg1 == Constants.PERMISSION_WRITE_EXTERNAL_STORAGE) {
                        service.checkMusicBase(framework.ourDaoMaster, msg.arg2 != 0);
                    }
                    break;
                case MSG_NOTICE:
                    Class c = (Class) msg.getData().getSerializable(Constants.KEY_NOTICE);
                    service.buildNotice(c);
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        MusicFramework.getInstance().handleAction(MusicView.COMPLETE);
    }

    @Override
    public void OnFrameworkError(int errorCode) {
        switch (errorCode) {
            case PLAYER_NULL:
                MusicFramework.getInstance().setPlayer(new PlayerHelper(this, this));
                break;
        }
    }

    private void buildNotice(Class c) {
        LogHelper.d(TAG, "set notice");
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_play_arrow_black_24dp);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, c);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(c);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    /**
     * send a Message to target activity
     */
    private void sendToTarget(int what, int arg1, int arg2) {
        sendToTarget(Message.obtain(null, what, arg1, arg2));
    }


    /**
     * send a Message to target activity
     */
    private void sendToTarget(Message msg) {
        if (replyTo == null) {
            return;
        }
        try {
            replyTo.send(msg);
        } catch (RemoteException e) {
            LogHelper.e(TAG, "无法向Activity发送消息", e);
        }
    }


    public class MusicIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                if (state == 0) {
                    LogHelper.d(TAG, "Headset is unplugged");

                }
            }
        }
    }

    private DisplayCallback displayCallback = new DisplayCallback() {
        @Override
        public void display(Music music) {
            LogHelper.i(TAG, "send music: " + music.toString());
            long musicID = music.getId();
            int id_a = (int) musicID;
            int id_b = (int) (musicID >> 32);
            sendToTarget(MSG_DISPLAY, id_a, id_b);

            if (mBuilder != null) {
                // notice bar
                mBuilder.setContentTitle(music.getTitle());
                mBuilder.setContentText(music.getArtist());

                Bitmap cover = BitmapFactory.decodeFile(music.getCoverPath());
                mBuilder.setLargeIcon(cover);
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                // mId allows you to update the notification later on.
                mNotificationManager.notify(noticeID, mBuilder.build());
            }
        }


        @Override
        public void refreshProgress(int progress) {
            sendToTarget(MSG_PROGRESS, progress, 0);
        }

        @Override
        public void feedback(int action_code) {
            sendToTarget(MSG_FEEDBACK, action_code, 0);
        }
    };


}




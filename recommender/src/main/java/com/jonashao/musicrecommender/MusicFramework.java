package com.jonashao.musicrecommender;


import android.os.Handler;
import android.os.Message;

import com.jonashao.musicrecommender.models.Cluster;
import com.jonashao.musicrecommender.models.ClusterDao;
import com.jonashao.musicrecommender.models.DaoMaster;
import com.jonashao.musicrecommender.models.DaoSession;
import com.jonashao.musicrecommender.models.Music;
import com.jonashao.musicrecommender.models.Recorder;
import com.jonashao.musicrecommender.models.RecorderDao;
import com.jonashao.musicrecommender.models.Subjection;
import com.jonashao.musicrecommender.strategies.BaseStrategy;
import com.jonashao.musicrecommender.util.DisplayCallback;
import com.jonashao.musicrecommender.util.LogHelper;


import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by Koche on 2016/3/10.
 * The center controller of the Music Recommender System
 */
public class MusicFramework {
    private static final String TAG = MusicFramework.class.getSimpleName();

    private static MusicFramework ourInstance = new MusicFramework();

    DisplayCallback displayCallback = null;
    OnErrorListener errorListener = null;

    BaseStrategy[] ourStrategies = new BaseStrategy[5];

    int strategiesCount = 0;
    PlayerHelper ourPlayer = null;
    DaoMaster ourDaoMaster = null;

    public static MusicFramework getInstance() {
        return ourInstance;
    }

    private MusicFramework() {
        addStrategy(new BaseStrategy(BaseStrategy.class.getSimpleName(), 100));
    }


    Handler mHandlerProgress = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (displayCallback != null && msg.what == MusicService.MSG_PROGRESS) {
                displayCallback.refreshProgress(ourPlayer.getPos());
            }
            return false;
        }
    });

    Runnable mRunnableDuration = new Runnable() {
        @Override
        public void run() {
            if (ourPlayer != null && ourPlayer.isPlaying()) {
                mHandlerProgress.sendEmptyMessage(MusicService.MSG_PROGRESS);
                mHandlerProgress.postDelayed(mRunnableDuration, TIME_UNIT);
            }
        }
    };
    private static final int TIME_UNIT = 300;

    public void setDisplayCallback(DisplayCallback callback) {
        displayCallback = callback;
        if (callback != null && ourPlayer != null) {
            Music now = ourPlayer.getNow();
            if (now != null) {
                displayCallback.display(now);
            }
        }
    }

    public void setPlayer(PlayerHelper player) {
        LogHelper.i(TAG, "Player set");
        ourPlayer = player;
    }

    public void setDaoMaster(DaoMaster daoMaster) {
        LogHelper.i(TAG, "DaoMaster set");
        ourDaoMaster = daoMaster;
        DaoSession daoSession = ourDaoMaster.newSession();

        long musicID = firstMusic(daoSession);
        LogHelper.d(TAG, "Prepare Source of " + musicID);
        if (musicID >= 0) {
            Music music = daoSession.getMusicDao().load(musicID);
            getPlayer().prepareSource(music);
            if (displayCallback != null) {
                displayCallback.display(music);
            }
        }
    }


    public void setErrorListener(OnErrorListener listener) {
        errorListener = listener;
    }


    public long firstMusic(DaoSession daoSession) {
        LogHelper.d(TAG, "recommend the first music");
        Cluster cluster = guessNow(daoSession);
        if (cluster == null) {
            // No permission to run the first scanning
            return -1;
        }

        if (cluster.getSubjectionList().size() == 0) {
            LogHelper.d(TAG, "cluster " + cluster.toString() + " contains no music, DELETE!");
            daoSession.getClusterDao().delete(cluster);
            LogHelper.d(TAG, "Use the Sea");
            cluster = daoSession.getClusterDao().load((long) 0);
        }

        List<Subjection> candidates = cluster.getSubjectionList();
        int size = candidates.size();
        int rangeRand = new Random().nextInt(10);

        if (rangeRand < 5) {
            candidates = candidates.subList(0, (int) (size * 0.2) + 1);
        } else if (rangeRand < 8) {
            candidates = candidates.subList((int) (size * 0.2) + 1, (int) (size * 0.8 + 1));
        } else {
            candidates = candidates.subList((int) (size * 0.8) + 1, size);
        }

        size = candidates.size();
        Random random = new Random();

        for (int i = 0; i < 5; i++) {
            int randID = random.nextInt(size);
            if (!Recorder.wasPlayed(randID)) {
                return randID;
            }
            LogHelper.d(TAG, "MusicID:" + randID + " once played, skip it.");
        }

        return -1;
    }

    public void addStrategy(BaseStrategy strategy) {
        LogHelper.i(TAG, "Add strategy:" + strategy.getName());
        int length = ourStrategies.length;
        if (strategiesCount >= length - 1) {
            BaseStrategy[] temp = new BaseStrategy[length * 2];
            System.arraycopy(ourStrategies, 0, temp, 0, ourStrategies.length);
            ourStrategies = temp;
        }

        for (int i = 0; i < strategiesCount; i++) {
            if (strategy.getWeight() > ourStrategies[i].getWeight()) {
                System.arraycopy(ourStrategies, 0, ourStrategies, i + 1, strategiesCount - i);
                ourStrategies[i] = strategy;
                strategiesCount++;
                return;
            }
        }

        ourStrategies[strategiesCount++] = strategy;
    }

    @SuppressWarnings("unused")
    public void removeStrategy(BaseStrategy strategy) {
        for (int i = 0; i < strategiesCount; i++) {
            if (ourStrategies[i].getName().equals(strategy.getName())) {
                strategiesCount--;
                System.arraycopy(ourStrategies, i + 1, ourStrategies, i, strategiesCount - i);
                return;
            }
        }
    }

    public PlayerHelper getPlayer() {
        return ourPlayer;
    }

    public DaoMaster getDaoMaster() {
        return ourDaoMaster;
    }

    private void play() throws IllegalStateException {
        ourPlayer.play();
        mHandlerProgress.postDelayed(mRunnableDuration, TIME_UNIT);
    }

    private void pause() throws IllegalStateException {
        ourPlayer.pause();
        mHandlerProgress.removeCallbacks(mRunnableDuration);
    }

    public void handleAction(int action_code) {
        DaoSession daoSession = ourDaoMaster.newSession();
        RecorderDao recorderDao = daoSession.getRecorderDao();

        Music music = ourPlayer.getNow();
        if (music != null) {
            Recorder recorder = new Recorder(null, action_code, new Date(), music.getId());
            recorder.writeHistory();
            recorderDao.insert(recorder);
        }

        if (ourPlayer == null) {
            errorListener.OnFrameworkError(OnErrorListener.PLAYER_NULL);
        } else {
            switch (action_code) {
                case MusicView.PLAY:
                    LogHelper.d(TAG, "play");
                    if (ourPlayer.getNow() != null) {
                        try {
                            play();
                        } catch (IllegalStateException e) {
                            LogHelper.e(TAG, "call play() in illegal state", e);
                        }
                    } else {
                        LogHelper.e(TAG, "ourPlayer or Now is null");
                    }
                    break;

                case MusicView.PAUSE:
                    LogHelper.d(TAG, "pause");
                    if (ourPlayer.isPlaying()) {
                        pause();
                    } else {
                        LogHelper.e(TAG, "pause() should be called when playing");
                    }
                    break;

                case MusicView.NEXT:
                    LogHelper.d(TAG, "next");
                    setDaoMaster(ourDaoMaster);
                    play();
                    break;

                case MusicView.LOVE:
                    LogHelper.d(TAG, "love");
                    break;

                case MusicView.COMPLETE:
                    LogHelper.d(TAG, "complete");
                    setDaoMaster(ourDaoMaster);
                    play();
                    break;
                default:
            }
        }

        for (int i = 0; i < strategiesCount; i++) {
            ourStrategies[i].analyzeAction(action_code);
        }
    }


    private Cluster guessNow(DaoSession session) {
        int hour = Calendar.getInstance().get(Calendar.HOUR);
        LogHelper.d(TAG, "guess Current cluster");
        ClusterDao clusterDao = session.getClusterDao();
        List<Cluster> clusters = clusterDao.queryBuilder().where(ClusterDao.Properties.Hour.eq(hour)).
                build().list();

        switch (clusters.size()) {
            case 0:
                LogHelper.d(TAG, "No cluster suit. use the sea. (Now is " + hour + " o'clock)");
                return clusterDao.load((long) 0);
            case 1:
                LogHelper.d(TAG, "Get the exact cluster:" + clusters.get(0).toString());
                return clusters.get(0);
            default:
                int rand = new Random().nextInt(clusters.size());
                LogHelper.d(TAG, "Random get a cluster: " + clusters.get(rand).toString());
                return clusters.get(rand);
        }
    }


    public interface OnErrorListener {
        int PLAYER_NULL = 1;

        void OnFrameworkError(int errorCode);
    }
}

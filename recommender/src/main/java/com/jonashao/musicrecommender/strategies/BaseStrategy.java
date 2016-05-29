package com.jonashao.musicrecommender.strategies;

import com.jonashao.musicrecommender.MusicFramework;
import com.jonashao.musicrecommender.MusicView;
import com.jonashao.musicrecommender.models.Cluster;
import com.jonashao.musicrecommender.models.ClusterDao;
import com.jonashao.musicrecommender.models.DaoSession;
import com.jonashao.musicrecommender.models.Recorder;
import com.jonashao.musicrecommender.models.Subjection;
import com.jonashao.musicrecommender.util.LogHelper;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * Created by Koche on 2016/3/10.
 *
 */
public class BaseStrategy {
    private static final String TAG = "BaseStrategy";
    private String name = null;
    private int weight = 0;

    public BaseStrategy(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWeight() {
        return weight;
    }

    @SuppressWarnings("unused")
    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void analyzeAction(int action_code) {
        MusicFramework framework = MusicFramework.getInstance();
        DaoSession daoSession = framework.getDaoMaster().newSession();


        switch (action_code) {
            case MusicView.PLAY:
                break;
            case MusicView.NEXT:
                //1. 降低dom

                break;

            case MusicView.LOVE:
                break;

            case MusicView.COMPLETE:

            default:
        }

    }


}

package com.jonashao.musicrecommender.models;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

import com.jonashao.musicrecommender.models.Subjection;
import com.jonashao.musicrecommender.models.Recorder;
import com.jonashao.musicrecommender.models.Music;
import com.jonashao.musicrecommender.models.Cluster;

import com.jonashao.musicrecommender.models.SubjectionDao;
import com.jonashao.musicrecommender.models.RecorderDao;
import com.jonashao.musicrecommender.models.MusicDao;
import com.jonashao.musicrecommender.models.ClusterDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig subjectionDaoConfig;
    private final DaoConfig recorderDaoConfig;
    private final DaoConfig musicDaoConfig;
    private final DaoConfig clusterDaoConfig;

    private final SubjectionDao subjectionDao;
    private final RecorderDao recorderDao;
    private final MusicDao musicDao;
    private final ClusterDao clusterDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        subjectionDaoConfig = daoConfigMap.get(SubjectionDao.class).clone();
        subjectionDaoConfig.initIdentityScope(type);

        recorderDaoConfig = daoConfigMap.get(RecorderDao.class).clone();
        recorderDaoConfig.initIdentityScope(type);

        musicDaoConfig = daoConfigMap.get(MusicDao.class).clone();
        musicDaoConfig.initIdentityScope(type);

        clusterDaoConfig = daoConfigMap.get(ClusterDao.class).clone();
        clusterDaoConfig.initIdentityScope(type);

        subjectionDao = new SubjectionDao(subjectionDaoConfig, this);
        recorderDao = new RecorderDao(recorderDaoConfig, this);
        musicDao = new MusicDao(musicDaoConfig, this);
        clusterDao = new ClusterDao(clusterDaoConfig, this);

        registerDao(Subjection.class, subjectionDao);
        registerDao(Recorder.class, recorderDao);
        registerDao(Music.class, musicDao);
        registerDao(Cluster.class, clusterDao);
    }
    
    public void clear() {
        subjectionDaoConfig.getIdentityScope().clear();
        recorderDaoConfig.getIdentityScope().clear();
        musicDaoConfig.getIdentityScope().clear();
        clusterDaoConfig.getIdentityScope().clear();
    }

    public SubjectionDao getSubjectionDao() {
        return subjectionDao;
    }

    public RecorderDao getRecorderDao() {
        return recorderDao;
    }

    public MusicDao getMusicDao() {
        return musicDao;
    }

    public ClusterDao getClusterDao() {
        return clusterDao;
    }

}

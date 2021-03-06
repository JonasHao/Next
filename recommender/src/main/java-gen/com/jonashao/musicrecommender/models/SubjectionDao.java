package com.jonashao.musicrecommender.models;

import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

import com.jonashao.musicrecommender.models.Subjection;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "SUBJECTION".
*/
public class SubjectionDao extends AbstractDao<Subjection, Long> {

    public static final String TABLENAME = "SUBJECTION";

    /**
     * Properties of entity Subjection.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property DegreeOfMembership = new Property(1, Float.class, "DegreeOfMembership", false, "DEGREE_OF_MEMBERSHIP");
        public final static Property MusicID = new Property(2, long.class, "musicID", false, "MUSIC_ID");
        public final static Property ClusterID = new Property(3, long.class, "clusterID", false, "CLUSTER_ID");
    };

    private Query<Subjection> music_SubjectionListQuery;
    private Query<Subjection> cluster_SubjectionListQuery;

    public SubjectionDao(DaoConfig config) {
        super(config);
    }
    
    public SubjectionDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"SUBJECTION\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"DEGREE_OF_MEMBERSHIP\" REAL," + // 1: DegreeOfMembership
                "\"MUSIC_ID\" INTEGER NOT NULL ," + // 2: musicID
                "\"CLUSTER_ID\" INTEGER NOT NULL );"); // 3: clusterID
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"SUBJECTION\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Subjection entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Float DegreeOfMembership = entity.getDegreeOfMembership();
        if (DegreeOfMembership != null) {
            stmt.bindDouble(2, DegreeOfMembership);
        }
        stmt.bindLong(3, entity.getMusicID());
        stmt.bindLong(4, entity.getClusterID());
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Subjection readEntity(Cursor cursor, int offset) {
        Subjection entity = new Subjection( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getFloat(offset + 1), // DegreeOfMembership
            cursor.getLong(offset + 2), // musicID
            cursor.getLong(offset + 3) // clusterID
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Subjection entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setDegreeOfMembership(cursor.isNull(offset + 1) ? null : cursor.getFloat(offset + 1));
        entity.setMusicID(cursor.getLong(offset + 2));
        entity.setClusterID(cursor.getLong(offset + 3));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Subjection entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Subjection entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "subjectionList" to-many relationship of Music. */
    public List<Subjection> _queryMusic_SubjectionList(long musicID) {
        synchronized (this) {
            if (music_SubjectionListQuery == null) {
                QueryBuilder<Subjection> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.MusicID.eq(null));
                queryBuilder.orderRaw("T.'DEGREE_OF_MEMBERSHIP' DESC");
                music_SubjectionListQuery = queryBuilder.build();
            }
        }
        Query<Subjection> query = music_SubjectionListQuery.forCurrentThread();
        query.setParameter(0, musicID);
        return query.list();
    }

    /** Internal query to resolve the "subjectionList" to-many relationship of Cluster. */
    public List<Subjection> _queryCluster_SubjectionList(long clusterID) {
        synchronized (this) {
            if (cluster_SubjectionListQuery == null) {
                QueryBuilder<Subjection> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.ClusterID.eq(null));
                queryBuilder.orderRaw("T.'DEGREE_OF_MEMBERSHIP' DESC");
                cluster_SubjectionListQuery = queryBuilder.build();
            }
        }
        Query<Subjection> query = cluster_SubjectionListQuery.forCurrentThread();
        query.setParameter(0, clusterID);
        return query.list();
    }

}

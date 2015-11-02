package com.jonashao.next.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.jonashao.next.util.LogHelper;

import java.net.URL;
import java.util.Random;

/**
 * Created by Junnan on 2015/10/30.
 * A singleton class for create and manipulate database
 */
public class DBOpenHelper extends SQLiteOpenHelper {
    private Context context;

    private static final String DB_NAME = "NextMusic";
    private static final int DB_VERSION = 1;

    private static final String TAG = "DB_Helper";

    private static final String MUSIC_INFO_TABLE_NAME = "music_info";
    private static final String MUSIC_FEATURES_TABLE_NAME = "music_features";
    private static final String MUSIC_NODE_TABLE_NAME = "music_node";
    private static final String MUSIC_NODE_DISLIKED_TABLE_NAME = "disliked_relation";
    private static final String PLAY_HISTORY_TABLE_NAME = "play_history";

    // Music Info
    public static final String KEY_ID = "music_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_ARTIST = "artist";
    // Music Features
    public static final String KEY_ALBUM = "album";
    public static final String KEY_YEAR = "year";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_GENRE = "genre";
    // Music Node
    public static final String KEY_LAST_ID = "last_id";
    public static final String KEY_NEXT_ID = "next_id";
    public static final String KEY_DISLIKE_ID = "dislike_id";
    public static final String KEY_DISLIKED_ID = "disliked_id";


    private static final String MUSIC_INFO_TABLE_CREATE =
            "CREATE TABLE " + MUSIC_INFO_TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY , " +
                    KEY_TITLE + " TEXT, " +
                    KEY_ARTIST + " TEXT," +
                    KEY_DURATION + " INTEGER); ";

    private static final String MUSIC_FEATURES_TABLE_CREATE =
            "CREATE TABLE " + MUSIC_FEATURES_TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY REFERENCES " + MUSIC_INFO_TABLE_NAME + "," +
                    KEY_ARTIST + " TEXT, " +
                    KEY_ALBUM + " TEXT, " +
                    KEY_YEAR + " INTEGER, " +
                    KEY_GENRE + " TEXT); ";

    private static final String MUSIC_NODE_TABLE_CREATE =
            "CREATE TABLE " + MUSIC_NODE_TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY REFERENCES " + MUSIC_INFO_TABLE_NAME + "," +
                    KEY_LAST_ID + " INTEGER REFERENCES " + MUSIC_INFO_TABLE_NAME + "," +
                    KEY_NEXT_ID + " INTEGER REFERENCES " + MUSIC_INFO_TABLE_NAME + "," +
                    KEY_DISLIKE_ID + " INTEGER REFERENCES " + MUSIC_INFO_TABLE_NAME + "); ";

    private static final String MUSIC_NODE_DISLIKED_TABLE_CREATE =
            "CREATE TABLE " + MUSIC_NODE_DISLIKED_TABLE_NAME + " (" +
                    KEY_ID + " INTEGER REFERENCES " + MUSIC_INFO_TABLE_NAME + "," +
                    KEY_DISLIKED_ID + " INTEGER REFERENCES " + MUSIC_INFO_TABLE_NAME + "); ";

    private static final String PLAY_HISTORY_TABLE_CREATE =
            "CREATE TABLE " + PLAY_HISTORY_TABLE_NAME + " (" +
                    KEY_ID + " INTEGER REFERENCES " + MUSIC_INFO_TABLE_NAME + "," +
                    KEY_LAST_ID + " INTEGER REFERENCES " + MUSIC_INFO_TABLE_NAME + "); ";


    private static DBOpenHelper instance;

    public static synchronized DBOpenHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBOpenHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    private DBOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MUSIC_INFO_TABLE_CREATE);
        db.execSQL(MUSIC_FEATURES_TABLE_CREATE);
        db.execSQL(MUSIC_NODE_TABLE_CREATE);
        db.execSQL(MUSIC_NODE_DISLIKED_TABLE_CREATE);
        db.execSQL(PLAY_HISTORY_TABLE_CREATE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + MUSIC_INFO_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + MUSIC_FEATURES_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + MUSIC_NODE_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + MUSIC_NODE_DISLIKED_TABLE_NAME);
            onCreate(db);
        }
    }

    // Insert or update a music into the database
    public long addOrUpdateMusic(MusicFeatures music) {
        MusicInfo musicInfo = music.getmMusicInfo();
        long musicID = musicInfo.getID();
        // The database connection is cached so it's not expensive to call getWritableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues info = new ContentValues();
            info.put(KEY_ID, musicID);
            info.put(KEY_TITLE, musicInfo.getTitle());
            info.put(KEY_ARTIST, musicInfo.getArtist());
            info.put(KEY_DURATION, musicInfo.getDuration());

            ContentValues features = new ContentValues();
            features.put(KEY_ID, musicID);
            features.put(KEY_ARTIST, musicInfo.getArtist());
            features.put(KEY_ALBUM, music.getAlbum());
            features.put(KEY_YEAR, music.getYear());
            features.put(KEY_GENRE, music.getGenre());

            // First try to update the music in case the music already exists in the database
            int rows = db.update(MUSIC_INFO_TABLE_NAME, info, KEY_ID + "= ?", new String[]{String.valueOf(musicID)});

            // Check if update succeeded
            if (rows == 1) {
                db.update(MUSIC_FEATURES_TABLE_NAME, features, KEY_ID + "= ?", new String[]{String.valueOf(musicID)});
                db.setTransactionSuccessful();
            } else {
                // user with this userName did not already exist, so insert new user
                db.insertOrThrow(MUSIC_INFO_TABLE_NAME, null, info);
                db.insertOrThrow(MUSIC_FEATURES_TABLE_NAME, null, features);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            LogHelper.d(TAG, "Error while trying to add or update user");
        } finally {
            db.endTransaction();
        }

        return musicID;
    }


    /**
     * get the latest played music from the played history.
     *
     * @return if query in table play_history success, return ID of the top MusicNode. else find a node randomly in see.
     */
    public synchronized long findLastPlayed() {
        long id = -1;
        String PLAY_HISTORY_QUERY = String.format("SELECT %s FROM %s", KEY_ID, PLAY_HISTORY_TABLE_NAME);
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(PLAY_HISTORY_QUERY, null);
        try {
            if (cursor.moveToLast()) {
                id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
                LogHelper.d(TAG, "找到上次播放的：" + id);
            } else {
                id = findRandomlyInSee();
                LogHelper.d(TAG, "没找到上次播放的，去大海随机寻找");
            }
        } catch (Exception e) {
            LogHelper.d(TAG, "从数据库获取播放历史记录失败");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return id;
    }

    /**
     * See means the set of music which do not have any user data with them.
     *
     * @return ID of a random node in see. return -1 when failed.
     */
    public synchronized long findRandomlyInSee() {
        long id = -1;

        String MUSIC_SEE_QUERY = String.format(
                "SELECT %s FROM %s " +
                        "WHERE %s NOT IN (" +
                        "SELECT %s FROM %s); ",
                KEY_ID, MUSIC_INFO_TABLE_NAME, KEY_ID,
                KEY_ID, MUSIC_NODE_TABLE_NAME);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(MUSIC_SEE_QUERY, null);

        try {
            int count = cursor.getCount();
            if (count <= 0) {
                //todo: 这个操作将很耗费计算
                LogHelper.d(TAG, "没查到大海的歌曲，将去扫描一遍曲库");
                MusicProvider provider = MusicProvider.getInstance(context);
                provider.scanMusic();
                cursor = db.rawQuery(MUSIC_SEE_QUERY, null);
                count = cursor.getCount();
            }
            if (count > 0) {
                // Generate a random number as the row number
                Random rand = new Random();
                int row = rand.nextInt(count);
                // get the id of that row
                if (cursor.moveToPosition(row)) {
                    id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
                }
            }

        } catch (Exception e) {
            LogHelper.e(TAG, "从数据库随机获取歌曲失败", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return id;
    }


    public synchronized MusicInfo queryMusicInfoByID(long ID) {
        MusicInfo musicInfo = null;
        String MUSIC_SELECT_QUERY = String.format(
                "SELECT * FROM %s " +
                        "WHERE %s = ?  ",
                MUSIC_INFO_TABLE_NAME, KEY_ID);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(MUSIC_SELECT_QUERY, new String[]{String.valueOf(ID)});

        try {
            if (cursor.moveToFirst()) {
                String title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(KEY_ARTIST));
                int duration = cursor.getInt(cursor.getColumnIndex(KEY_DURATION));
                LogHelper.d(TAG,"duration: "+duration +" column: "+cursor.getColumnIndex(KEY_DURATION));
                musicInfo = new MusicInfo(ID, title, artist);
                musicInfo.setDuration(duration);
            }
        } catch (Exception e) {
            LogHelper.e(TAG, "从数据库获取歌曲失败", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return musicInfo;
    }

    public synchronized Uri completionNext(){
        int id = -1;

        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        return contentUri;
    }


}


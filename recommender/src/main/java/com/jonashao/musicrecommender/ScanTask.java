package com.jonashao.musicrecommender;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.provider.MediaStore;

import com.jonashao.musicrecommender.models.Cluster;
import com.jonashao.musicrecommender.models.DaoSession;
import com.jonashao.musicrecommender.models.Music;
import com.jonashao.musicrecommender.models.MusicDao;
import com.jonashao.musicrecommender.models.Subjection;
import com.jonashao.musicrecommender.models.SubjectionDao;
import com.jonashao.musicrecommender.util.LogHelper;

import java.util.HashSet;
import java.util.Set;

/**
 * The task to scan music from disk.
 */
public class ScanTask {
    private static final int MIN_DURATION = 45000;
    public static String TAG = ScanTask.class.getSimpleName();

    Context context;
    DaoSession session;

    public ScanTask(Context context, DaoSession daoSession) {
        this.context = context;
        this.session = daoSession;
    }


    public void scanMusic() throws SQLException {
        LogHelper.i(TAG, "scan music");
        Set<Music> musics = new HashSet<>();

        ContentResolver contentResolver = context.getContentResolver();
        Uri media_uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri album_uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

//        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        Cursor cursor = contentResolver.query
                (media_uri, new String[]{
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ARTIST_ID,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.YEAR,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.IS_MUSIC,
                }, null, null, null);

        if (cursor == null) {
            LogHelper.e(TAG, "扫描本地歌曲失败");
        } else if (!cursor.moveToFirst()) {
            cursor.close();
        } else {
            int total = cursor.getCount();
            LogHelper.i(TAG, "Cursor count:" + total);

            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int artistIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID);
            int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
            int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
//            int isMusicColumn = cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC);

            /** All musics belongs to sea when init*/
            Cluster sea = new Cluster((long) 0);
            session.getClusterDao().insert(sea);

            MusicDao musicDao = session.getMusicDao();
            SubjectionDao subjectionDao = session.getSubjectionDao();

            do {
                String thisTitle = cursor.getString(titleColumn);
                int thisId = cursor.getInt(idColumn);
                String thisArtist = cursor.getString(artistColumn);
                long thisArtistId = cursor.getLong(artistIdColumn);
                int thisDuration = cursor.getInt(durationColumn);
                int thisYear = cursor.getInt(yearColumn);
                String thisAlbum = cursor.getString(albumColumn);
                long thisAlbumId = cursor.getLong(albumIdColumn);

                if (thisArtist.equals("<unknown>")) {
                    thisArtist = context.getString(R.string.unknown_artist);
                }

                if (thisDuration < MIN_DURATION) {
                    LogHelper.d(TAG, "时长为：" + thisDuration + "，太小了");
                    continue;
                }

                Music music = new Music(null, false, thisId, thisTitle, thisArtist,
                        thisArtistId, null, thisDuration, thisAlbum, thisAlbumId, thisYear, null);

                musicDao.insert(music);
                // init subjection relationship with DOM equals to 100(absolutely belong to)
                Subjection subjection = new Subjection(null, (float) 100, music.getId(), 0);
                subjectionDao.insert(subjection);

                music.getSubjectionList().add(subjection);
                sea.getSubjectionList().add(subjection);

                musics.add(music);

            } while (cursor.moveToNext());
            cursor.close();

            for (Music music : musics) {
//                LogHelper.d(TAG, "为RID:" + music.getRID() + "添加封面");
                try {
                    Cursor AlbumCursor = contentResolver.query(
                            album_uri, new String[]{
                                    MediaStore.Audio.Albums._ID,
                                    MediaStore.Audio.Albums.ALBUM_KEY,
                                    MediaStore.Audio.Albums.ALBUM_ART},
                            MediaStore.Audio.Albums._ID + " = ?",
                            new String[]{String.valueOf(music.getAlbumID())}, null);
                    if (AlbumCursor == null) {
                        LogHelper.e(TAG, "扫描本地封面库失败");
                    } else {
                        int coverPathColumn = AlbumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
                        if (AlbumCursor.moveToFirst()) {
                            String coverPath = AlbumCursor.getString(coverPathColumn);
                            music.setCoverPath(coverPath);
                            LogHelper.d(TAG, "CoverPath:" + coverPath);
                        }
                        AlbumCursor.close();
                    }
                } catch (SQLException e) {
                    LogHelper.e(TAG, "ALBUM FAILED", e);
                }
            }

            session.getMusicDao().updateInTx(musics);

        }
    }

}

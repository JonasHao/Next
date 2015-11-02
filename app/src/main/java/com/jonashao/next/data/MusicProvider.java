package com.jonashao.next.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.jonashao.next.R;
import com.jonashao.next.util.LogHelper;

/**
 * Created by Junnan on 2015/10/30.
 * Helper class for scanning local music
 */
public class MusicProvider {
    private static final String TAG = "MusicProvider";
    final Context mContext;
    DBOpenHelper NextDB;


    private MusicProvider(Context context) {
        mContext = context;
        NextDB = DBOpenHelper.getInstance(mContext);
    }
    private static MusicProvider instance;

    public static synchronized MusicProvider getInstance(Context context) {
        if (instance == null) {
            instance = new MusicProvider(context.getApplicationContext());
        }
        return instance;
    }



    public void scanMusic() {
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null) {
            LogHelper.e(TAG,"扫描本地歌曲失败");
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(mContext,mContext.getString(R.string.no_music),Toast.LENGTH_SHORT).show();
        } else {
            int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR);

            do {
                long thisId = cursor.getLong(idColumn);
                String thisTitle = cursor.getString(titleColumn);
                String thisArtist = cursor.getString(artistColumn);
                String thisAlbum = cursor.getString(albumColumn);
                int thisDuration = cursor.getInt(durationColumn);
                int thisYear = cursor.getInt(yearColumn);

                if(thisArtist.equals("<unknown>")){
                    thisArtist = mContext.getString(R.string.unknown_artist);
                }

                MusicFeatures musicFeatures = new MusicFeatures(thisId,thisTitle,thisArtist,thisDuration);
                musicFeatures.setAlbum(thisAlbum);
                musicFeatures.setYear(thisYear);

                NextDB.addOrUpdateMusic(musicFeatures);
                LogHelper.v(TAG,"歌名："+thisTitle + " 歌手："+thisArtist);
            } while (cursor.moveToNext());
        }
    }
}



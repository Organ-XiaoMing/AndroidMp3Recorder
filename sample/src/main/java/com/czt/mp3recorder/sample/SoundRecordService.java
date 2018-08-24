package com.czt.mp3recorder.sample;

import com.czt.mp3recorder.sample.R;

import android.app.AlertDialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.czt.mp3recorder.MP3Recorder;
import com.czt.mp3recorder.sample.bean.Constant;
import com.czt.mp3recorder.sample.util.LogUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ming.Xiao on 2018/8/21.
 */

public class SoundRecordService extends Service implements MP3Recorder.Mp3RecorderListener{

    public static final String TAG = "SoundRecordService";
    public static final int STATE_IDLE = 1;
    public static final int STATE_RECORDING = 2;
    public static final int STATE_PAUSE_RECORDING = 3;
    public static final int STATE_SAVE_RECORDING = 4;
    private int mCurrentState = STATE_IDLE;
    public static final String HANDLER_THREAD_NAME = "SoundRecorderServiceHandler";

    private SoundRecorderBinder mBinder = new SoundRecorderBinder();

    private MP3Recorder mMp3Record;
    SoundRecorderServiceHandler  mSoundRecorderServiceHandler;
    private HandlerThread mHandlerThread = null;

    private RecorderListener mListener;

    private Uri mUri;
    private static final long FACTOR_FOR_SECOND_AND_MINUTE = 1000;
    private long mCurrentFileDuration = -1;
    private long mTotalRecordingDuration = -1;

    private static final int PLAYLIST_ID_NULL = -1;
    private static final String ALBUM_RECORDER = "recorder";

    @Override
    public void onCreate() {
        super.onCreate();

        mMp3Record = new MP3Recorder();
        mMp3Record.setListener(this);

        mHandlerThread = new HandlerThread(HANDLER_THREAD_NAME);
        mHandlerThread.start();

        mSoundRecorderServiceHandler = new SoundRecorderServiceHandler(mHandlerThread.getLooper());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.v(TAG, "<onBind>");
        return mBinder;
    }

    public void setListener(RecorderListener listener){
        this.mListener = listener;
    }

    public void setState(int state){
        this.mCurrentState = state;
        mListener.onStateChanged(state);
    }

    public void startRecordingAsync(File file){
        LogUtils.v(TAG, "<startRecordingAsync>");
        if(mCurrentState == STATE_IDLE){
            LogUtils.v(TAG, "<startRecordingAsync> new MP3 File");
            mMp3Record.initFile(file);
        }
        sendThreadHandlerMessage(SoundRecorderServiceHandler.START_REOCRD);
    }

    public void onresumeRecordingAsync(){
        LogUtils.v(TAG, "<onresumeRecordingAsync>");
        sendThreadHandlerMessage(SoundRecorderServiceHandler.ONRESUME_RECORD);
    }

    public void pauseRecordingAsync() {
        LogUtils.v(TAG, "<pauseRecordingAsync>");
        sendThreadHandlerMessage(SoundRecorderServiceHandler.PAUSE_REOCRD);
    }

    public void saveRecordAsync() {
        LogUtils.v(TAG, "<saveRecordAsync>");
        sendThreadHandlerMessage(SoundRecorderServiceHandler.SAVE_RECORD);
    }

    private void sendThreadHandlerMessage(int what) {
        mSoundRecorderServiceHandler.removeCallbacks(mHandlerThread);
        mSoundRecorderServiceHandler.sendEmptyMessage(what);
    }

    @Override
    public void onStateChanged(int stateCode) {
        LogUtils.v(TAG,"onStateChanged " +stateCode);
        setState(stateCode);
    }

    public  class  SoundRecorderServiceHandler extends Handler{

        public SoundRecorderServiceHandler(Looper looper) {
            super(looper);
        }

        public static final int START_REOCRD = 0;
        public static final int PAUSE_REOCRD = 1;
        public static final int STOP_REOCRD = 2;
        public static final int SAVE_RECORD = 3;
        public static final int ONRESUME_RECORD = 4;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case START_REOCRD:
                    startRecord();
                    break;
                case PAUSE_REOCRD:
                    pauseRecord();
                    break;
                case STOP_REOCRD:
                    stopRecord();
                    break;
                case SAVE_RECORD:
                    saveRecord();
                    break;
                case ONRESUME_RECORD:
                    onresumeRecord();
                    break;
            }
        }
    }

    public void startRecord(){
            try {
                mMp3Record.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void onresumeRecord(){
            mMp3Record.onresume();
    }

    public void stopRecord(){
        mMp3Record.stop();
    }

    public void saveRecord(){
        mMp3Record.stop();
        File currentFile = mMp3Record.getRecordFile();
        if(currentFile == null){
            LogUtils.v(TAG,"currentFile is no exist");
            return;
        }
        mTotalRecordingDuration = 0;
        mUri = addToMediaDB(currentFile);
        mMp3Record.restart();

    }

    public void pauseRecord(){
        mMp3Record.pause();
        setState(STATE_PAUSE_RECORDING);
    }

    private Uri addToMediaDB(File file){
        LogUtils.v(TAG, "<addToMediaDB> begin");
        if (null == file) {
            LogUtils.v(TAG, "<addToMediaDB> file is null, return null");
            return null;
        }
        Resources res = getResources();
        long current = System.currentTimeMillis();
        Date date = new Date(current);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(
                R.string.audio_db_title_format));
        String title = simpleDateFormat.format(date);
        final int size = 8;
        ContentValues cv = new ContentValues(size);
        cv.put(MediaStore.Audio.Media.IS_MUSIC, "1");
        cv.put(MediaStore.Audio.Media.TITLE, title);
        cv.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / FACTOR_FOR_SECOND_AND_MINUTE));
        LogUtils.v(TAG, "<addToMediaDB> File type is " + Constant.AUDIO_NOT_LIMIT_TYPE);
        cv.put(MediaStore.Audio.Media.MIME_TYPE,  Constant.AUDIO_NOT_LIMIT_TYPE);
        cv.put(MediaStore.Audio.Media.ARTIST, res.getString(R.string.unknown_artist_name));
        cv.put(MediaStore.Audio.Media.ALBUM, res.getString(R.string.audio_db_album_name));
        cv.put(MediaStore.Audio.Media.DATA, file.getAbsolutePath());
        cv.put(MediaStore.Audio.Media.DURATION, mTotalRecordingDuration);
        cv.put("album_artist", ALBUM_RECORDER);
        LogUtils.v(TAG, "<addToMediaDB> Reocrding time output to database is :DURATION= "
                + mCurrentFileDuration);
        ContentResolver resolver = getContentResolver();
        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri result = null;
        /** M: add exception process @{ */
        try {
            result = resolver.insert(base, cv);
        } catch (UnsupportedOperationException e) {
            LogUtils.v(TAG, "<addToMediaDB> Save in DB failed: " + e.getMessage());
        }
        /** @} */
        if (null == result) {
            LogUtils.v(TAG, "<addToMediaDB> Save failed in DB");
        } else {
            LogUtils.v(TAG, "<addToMediaDB> Save susceeded in DB");
            Toast.makeText(SoundRecordService.this,R.string.record_save_message,Toast.LENGTH_SHORT).show();
            if (PLAYLIST_ID_NULL == getPlaylistId(res)) {
                createPlaylist(res, resolver);
            }
            int audioId = Integer.valueOf(result.getLastPathSegment());
            if (PLAYLIST_ID_NULL != getPlaylistId(res)) {
                addToPlaylist(resolver, audioId, getPlaylistId(res));
            }
            // Notify those applications such as Music listening to the
            // scanner events that a recorded audio file just created.
            /**
             * M: use MediaScanner to scan record file just be added, replace
             * send broadcast to scan all
             */
        }
        return result;
    }

    /*
    * Obtain the id for the default play list from the audio_playlists table.
    */
    private int getPlaylistId(Resources res) {
        Uri uri = MediaStore.Audio.Playlists.getContentUri("external");
        final String[] ids = new String[] {
                MediaStore.Audio.Playlists._ID
        };
        final String where = MediaStore.Audio.Playlists.NAME + "=?";
        final String[] args = new String[] {
                res.getString(R.string.audio_db_playlist_name)
        };
        Cursor cursor = query(uri, ids, where, args, null);
        if (cursor == null) {
            Log.v(TAG, "query returns null");
        }
        int id = -1;
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                id = cursor.getInt(0);
            }
            cursor.close();
        }
        return id;
    }

    /*
      * Create a playlist with the given default playlist name, if no such
      * playlist exists.
      */
    private Uri createPlaylist(Resources res, ContentResolver resolver) {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Audio.Playlists.NAME, res.getString(R.string.audio_db_playlist_name));
        Uri uri = resolver.insert(MediaStore.Audio.Playlists.getContentUri("external"), cv);
        if (uri == null) {
            Log.d(TAG, "createPlaylist: failed");
        }
        return uri;
    }

    /*
    * Add the given audioId to the playlist with the given playlistId; and
    * maintain the play_order in the playlist.
    */
    private void addToPlaylist(ContentResolver resolver, int audioId, long playlistId) {
        String[] cols = new String[] {
                "count(*)"
        };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cur = resolver.query(uri, cols, null, null, null);
        cur.moveToFirst();
        final int base = cur.getInt(0);
        cur.close();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Integer.valueOf(base + audioId));
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
        resolver.insert(uri, values);
    }

    /*
 * A simple utility to do a query into the databases.
 */
    private Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                         String sortOrder) {
        try {
            ContentResolver resolver = getContentResolver();
            if (resolver == null) {
                return null;
            }
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (UnsupportedOperationException ex) {
            return null;
        }
    }

    public interface RecorderListener {
        void onStateChanged(int stateCode);
    }

    public class SoundRecorderBinder extends Binder {
      public  SoundRecordService getService() {
            return SoundRecordService.this;
        }
    }

}

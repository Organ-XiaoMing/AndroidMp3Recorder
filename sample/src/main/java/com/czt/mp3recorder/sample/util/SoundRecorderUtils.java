package com.czt.mp3recorder.sample.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by Ming.Xiao on 2018/8/23.
 */

public class SoundRecorderUtils {

    public static final String TAG = "SoundRecorderUtils";

    /**
     * delete file from Media database
     *
     * @param context
     *            the context that call this function
     * @param filePath
     *            the file path to to deleted
     */
    public static boolean deleteFileFromMediaDB(Context context, String filePath) {
        LogUtils.v(TAG, "<deleteFileFromMediaDB> begin");
        if (null == context) {
            LogUtils.v(TAG, "<deleteFileFromMediaDB> context is null");
            return false;
        }
        if (null == filePath) {
            LogUtils.v(TAG, "<deleteFileFromMediaDB> filePath is null");
            return false;
        }
        ContentResolver resolver = context.getContentResolver();
        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String[] ids = new String[] { MediaStore.Audio.Media._ID };
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(MediaStore.Audio.Media.DATA);
        stringBuilder.append(" LIKE '%");
        stringBuilder.append(filePath.replaceFirst("file:///", ""));
        stringBuilder.append("'");
        final String where = stringBuilder.toString();
        Cursor cursor = query(context, base, ids, where, null, null);
        boolean res = false;
        try {
            if ((null != cursor) && (cursor.getCount() > 0)) {
                int deleteNum = resolver.delete(base, where, null);
                LogUtils.v(TAG, "<deleteFileFromMediaDB> delete " + deleteNum + " items in db");
                res = (deleteNum != 0);
            } else {
                if (cursor == null) {
                    LogUtils.v(TAG, "<deleteFileFromMediaDB>, cursor is null");
                } else {
                    LogUtils.v(TAG, "<deleteFileFromMediaDB>, cursor is:" + cursor
                            + "; cursor.getCount() is:" + cursor.getCount());
                }
            }
        } catch (IllegalStateException e) {
            LogUtils.v(TAG, "<deleteFileFromMediaDB> " + e.getMessage());
            res = false;
        } catch (SQLiteFullException e) {
            LogUtils.v(TAG, "<deleteFileFromMediaDB> " + e.getMessage());
            res = false;
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        LogUtils.v(TAG, "<deleteFileFromMediaDB> end");
        return res;
    }

    /**
     * A simple utility to do a query into the databases
     *
     * @param context
     *            the context that call this function
     * @param uri
     *            data URI
     * @param projection
     *            column collection
     * @param selection
     *            the rule of select
     * @param selectionArgs
     *            the args of select
     * @param sortOrder
     *            sort order
     * @return the cursor returned by resolver.query
     */
    public static Cursor query(Context context, Uri uri, String[] projection, String selection,
                               String[] selectionArgs, String sortOrder) {
        if (null == context) {
            LogUtils.v(TAG, "<query> context is null");
            return null;
        }
        try {
            ContentResolver resolver = context.getContentResolver();
            if (null == resolver) {
                LogUtils.v(TAG, "<query> resolver is null");
                return null;
            }
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (UnsupportedOperationException ex) {
            LogUtils.v(TAG, ex.getMessage());
            return null;
        }
    }

}

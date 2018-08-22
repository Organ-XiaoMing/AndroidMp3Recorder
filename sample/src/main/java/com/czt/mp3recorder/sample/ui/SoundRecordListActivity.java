package com.czt.mp3recorder.sample.ui;

import com.czt.mp3recorder.sample.R;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.czt.mp3recorder.sample.adapter.RecordListAdapter;
import com.czt.mp3recorder.sample.util.LogUtils;
import com.czt.mp3recorder.sample.view.HeadActionbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ming.Xiao on 2018/8/22.
 */

public class SoundRecordListActivity extends BaseActivity{

    private static final String TAG ="SoundRecordListActivity";

    private final ArrayList<HashMap<String, Object>> mArrlist = new ArrayList<HashMap<String, Object>>();
    private final ArrayList<String> mNameList = new ArrayList<String>();
    private final ArrayList<String> mPathList = new ArrayList<String>();
    private final ArrayList<String> mTitleList = new ArrayList<String>();
    private final ArrayList<String> mDurationList = new ArrayList<String>();
    private final List<Long> mIdList = new ArrayList<Long>();
    private List<Integer> mCheckedList = new ArrayList<Integer>();

    private QueryDataTask mQueryTask;
    private static final int ONE_SECOND_LIST = 1000;
    public static final int NORMAL = 1;
    private int mCurrentAdapterMode = NORMAL;

    private static final int NO_CHECK_POSITION = -1;
    private static final int DEFAULT_SLECTION = -1;

    private static final int PATH_INDEX = 2;
    private static final int DURATION_INDEX = 3;
    private static final int CREAT_DATE_INDEX = 5;
    private static final int RECORD_ID_INDEX = 7;
    private static final String DURATION = "duration";
    private static final String FILE_NAME = "filename";
    private static final String CREAT_DATE = "creatdate";
    private static final String FORMAT_DURATION = "formatduration";
    private static final String RECORD_ID = "recordid";
    private static final String RECORD_NOW_PLAYING = "nowplaying";
    private static final String RECORD_POINT = "point";
    private static final String RECORD_POINT_IS_SHOW = "is_need_show_tag";

    private static final String AUDIO_NOT_LIMIT_TYPE = "audio/*";
    private static final String DIALOG_TAG_SELECT_MODE = "SelectMode";
    private static final String DIALOG_TAG_SELECT_FORMAT = "SelectFormat";
    private static final String DIALOG_TAG_SELECT_EFFECT = "SelectEffect";
    private static final String SOUND_RECORDER_DATA = "sound_recorder_data";
    private static final String PATH = "path";
    public static final String PLAY = "play";
    public static final String RECORD = "record";
    public static final String INIT = "init";
    public static final String DOWHAT = "dowhat";
    public static final String EMPTY = "";
    public static final String ERROR_CODE = "errorCode";

    private ListView lViewRecord;
    private TextView mTextView;
    private HeadActionbar mActionbar;

    private RecordListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_list);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setListData(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView(){
        lViewRecord = (ListView) findViewById(R.id.record_list);
        mTextView = (TextView) findViewById(R.id.select);
        mActionbar = (HeadActionbar) findViewById(R.id.action_bar);
        mActionbar.setTitle(R.string.record_list_title);
    }

    /**
     * bind data to list view
     *
     * @param list the index list of current checked items
     */
    private void setListData(List<Integer> list) {
        LogUtils.v(TAG, "<setListData>" +lViewRecord);
        lViewRecord.setAdapter(null);
        if (mQueryTask != null) {
            mQueryTask.cancel(false);
        }
        mQueryTask = new QueryDataTask(list);
        mQueryTask.execute();
    }

    /**
     * query sound recorder recording file data
     *
     * @return the query list of the map from String to Object
     */
    public ArrayList<HashMap<String,Object>> queryData(){
        LogUtils.v(TAG, "<queryData>");

        mArrlist.clear();
        mNameList.clear();
        mPathList.clear();
        mTitleList.clear();
        mDurationList.clear();
        mIdList.clear();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(MediaStore.Audio.Media.IS_MUSIC);
        stringBuilder.append(" =1");
        String selection = stringBuilder.toString();
        Cursor recordingFileCursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATE_ADDED,
                        MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media._ID
                }, selection, null, MediaStore.Audio.Media.DATE_ADDED + " desc");

        try {
            if ((null == recordingFileCursor) || (0 == recordingFileCursor.getCount())) {
                LogUtils.v(TAG, "<queryData> the data return by query is null");
                return null;
            }
            LogUtils.v(TAG, "<queryData> the data return by query is available");
            recordingFileCursor.moveToFirst();
            int num = recordingFileCursor.getCount();
            final int sizeOfHashMap = 9;
            String path = null;
            String fileName = null;
            int duration = 0;
            long cDate = 0;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(
                    R.string.audio_db_title_format));
            String createDate = null;
            long recordId;
            Date date = new Date();
            int tagCount = 0;
            for (int j = 0; j < num; j++) {
                HashMap<String, Object> map = new HashMap<String, Object>(sizeOfHashMap);
                path = recordingFileCursor.getString(PATH_INDEX);
                if (null != path) {
                    fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
                }
                duration = recordingFileCursor.getInt(DURATION_INDEX);
                if (duration < ONE_SECOND_LIST) {
                    duration = ONE_SECOND_LIST;
                }
                cDate = recordingFileCursor.getInt(CREAT_DATE_INDEX);
                date.setTime(cDate * ONE_SECOND_LIST);
                createDate = simpleDateFormat.format(date);
                recordId = recordingFileCursor.getInt(RECORD_ID_INDEX);
                map.put(FILE_NAME, fileName);
                map.put(PATH, path);
                map.put(DURATION, duration);
                map.put(CREAT_DATE, createDate);
                map.put(FORMAT_DURATION, showTimeCount(duration));
                map.put(RECORD_ID, recordId);
                map.put(RECORD_NOW_PLAYING, false);
                map.put(RECORD_POINT, tagCount + "");
                map.put(RECORD_POINT_IS_SHOW, tagCount > 0 ? true : false);
                mNameList.add(fileName);
                mPathList.add(path);
                mTitleList.add(createDate);
                mDurationList.add(showTimeCount(duration));
                mIdList.add(recordId);
                recordingFileCursor.moveToNext();
                mArrlist.add(map);
                Log.d(TAG, "queryData: " +fileName);

            }
        }catch (IllegalStateException e){
            LogUtils.v(TAG,"queryData failed");
        }finally {
            if(recordingFileCursor!=null){
                recordingFileCursor.close();
            }
        }
        return mArrlist;
    }

    public void afterQuery(List<Integer> list){
        LogUtils.v(TAG, "<afterQuery>");
        if (null == list) {
            mCurrentAdapterMode = NORMAL;
            swicthAdapterView(NO_CHECK_POSITION);
        } else {
            list.retainAll(mIdList);
            if (list.isEmpty()) {
                mCurrentAdapterMode = NORMAL;
            }
        }
    }

    /**
     * switch adapter mode between NORMAL and EDIT
     *
     * @param pos the index of current clicked item
     */
    public void swicthAdapterView(int pos) {
        if (NORMAL == mCurrentAdapterMode) {
            LogUtils.v(TAG, "<swicthAdapterView> from edit mode to normal mode" +lViewRecord);
            mAdapter = new RecordListAdapter(this, mArrlist);
            lViewRecord.setAdapter(mAdapter);
        }
    }

    public String showTimeCount(long time) {
        time = time / 1000;
        String timeCount;
        long hourc = time / 3600;
        String hour = "0" + hourc;
        hour = hour.substring(hour.length() - 2, hour.length());
        long minuec = (time - hourc * 3600) / 60;
        String minue = "0" + minuec;
        minue = minue.substring(minue.length() - 2, minue.length());
        long secc = (time - hourc * 3600 - minuec * 60);
        String sec = "0" + secc;
        sec = sec.substring(sec.length() - 2, sec.length());
        timeCount = hour + ":" + minue + ":" + sec;
        return timeCount;
    }

    public  class QueryDataTask extends AsyncTask<Void,Object,ArrayList<HashMap<String,Object>>>{

        List<Integer> mList;

        public  QueryDataTask(List<Integer> list){
          mList = list;
        }

        /**
         * query data from database
         *
         * @param params no parameter
         * @return the query result
         */
        @Override
        protected ArrayList<HashMap<String, Object>> doInBackground(Void... params) {
            LogUtils.v(TAG, "<QueryDataTask.doInBackground>");
            return queryData();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LogUtils.v(TAG, "<QueryDataTask.onPostExecute>");
            if(mQueryTask == QueryDataTask.this){
                mQueryTask = null;
            }
            if(QueryDataTask.this.isCancelled()){
                LogUtils.v(TAG, "<QueryDataTask.onPostExceute> task is cancelled, return.");
                return;
            }
            afterQuery(mList);
        }
    }
}

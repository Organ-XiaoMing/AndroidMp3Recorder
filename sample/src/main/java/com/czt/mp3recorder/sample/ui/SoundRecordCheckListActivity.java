package com.czt.mp3recorder.sample.ui;

import com.czt.mp3recorder.sample.R;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.czt.mp3recorder.sample.adapter.RecordCheckListAdapter;
import com.czt.mp3recorder.sample.adapter.RecordListAdapter;
import com.czt.mp3recorder.sample.util.LogUtils;
import com.czt.mp3recorder.sample.util.SoundRecorderUtils;
import com.czt.mp3recorder.sample.view.HeadActionbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ming.Xiao on 2018/8/22.
 */

public class SoundRecordCheckListActivity extends BaseActivity implements View.OnClickListener,ListView.OnItemClickListener{

    private static final String TAG ="SoundRecordCheckListActivity";

    private final ArrayList<HashMap<String, Object>> mArrlist = new ArrayList<HashMap<String, Object>>();
    private final ArrayList<String> mNameList = new ArrayList<String>();
    private final ArrayList<String> mPathList = new ArrayList<String>();
    private final ArrayList<String> mTitleList = new ArrayList<String>();
    private final ArrayList<String> mDurationList = new ArrayList<String>();
    private final List<Long> mIdList = new ArrayList<Long>();
    private List<String> mCheckedList = new ArrayList<String>();

    private QueryDataTask mQueryTask;
    private static final int ONE_SECOND_LIST = 1000;
    public static final int NORMAL = 1;
    private int mCurrentAdapterMode = NORMAL;

    private static final int NO_CHECK_POSITION = -1;

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

    private static final String RECORD_CHECK = "record_check";
    private static final String PATH = "path";

    private ListView lViewRecord;
    private TextView mRecordCheckAll;
    private TextView mRecordDelete;
    private HeadActionbar mActionbar;
    private RecordCheckListAdapter mAdapter;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_check_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
        setListData(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        reset();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.record_check_all:
                doSelectAll();
                break;
            case R.id.record_delete:
                doDelete();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             LogUtils.v(TAG,"onItemClick begin" );
             if(!isEmptyList(mArrlist)){
                 boolean isChecked = (boolean) mArrlist.get(position).get(RECORD_CHECK);
                 LogUtils.v(TAG,"the position is " +isChecked);
                 mArrlist.get(position).put(RECORD_CHECK,!isChecked);
                 boolean isNewChecked = (boolean) mArrlist.get(position).get(RECORD_CHECK);
                 String tempPath = mPathList.get(position);
                 if(isNewChecked&&!mCheckedList.contains(tempPath)){
                     LogUtils.v(TAG, "mCheckedList add " +tempPath);
                     mCheckedList.add(tempPath);
                 }else{
                     if(mCheckedList.contains(tempPath)){
                         LogUtils.v(TAG, "mCheckedList remove " +tempPath);
                         mCheckedList.remove(tempPath);
                     }
                 }
                 mAdapter.notifyDataSetChanged();
             }
    }

    private void initView(){
        lViewRecord = (ListView) findViewById(R.id.record_list);
        lViewRecord.setOnItemClickListener(this);
        mRecordCheckAll = (TextView) findViewById(R.id.record_check_all);
        mRecordCheckAll.setOnClickListener(this);
        mRecordDelete = (TextView) findViewById(R.id.record_delete);
        mRecordDelete.setOnClickListener(this);
        mActionbar = (HeadActionbar) findViewById(R.id.action_bar);
        mActionbar.setTitle(R.string.record_check_list_title);
    }

    public void doSelectAll(){
        LogUtils.v(TAG,"doSelectAll begin");
        if(isEmptyList(mArrlist)){
            LogUtils.v(TAG,"the list is empty");
            return;
        }else{
            for (int i =0;i<mArrlist.size();i++){
                 mArrlist.get(i).put(RECORD_CHECK,true);
                 mCheckedList.add(mPathList.get(i));
                 if(mAdapter!=null){
                     mAdapter.notifyDataSetChanged();
                 }
            }
        }
    }

    public void doDelete(){
        LogUtils.v(TAG,"doDelete begin");
        FileDeleteTask fileDeleteTask=new FileDeleteTask();
        fileDeleteTask.execute();
    }

    public void reset(){
        if(!isEmptyList(mArrlist)){
            mArrlist.clear();;
        }
        if(!isEmptyList(mCheckedList)){
            mCheckedList.clear();
        }
        if(!isEmptyList(mPathList)){
            mPathList.clear();
        }
    }

    public static boolean isEmptyList(List list){
        if(list != null&&list.size()>0){
            return  false;
        }
        return true;
    }

    public List<File> getSelectedFiles(){
        List<File> list = new ArrayList<File>();
        int listSize = mCheckedList.size();
        LogUtils.v(TAG, "getSelectedFiles: listSize" +listSize);
        for (int i = 0; i < listSize; i++) {
            File file = new File(mCheckedList.get(i));
            LogUtils.v(TAG,"getSelectedFiles add " +file);
            list.add(file);
        }
        return list;
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
        mCheckedList.clear();

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
                map.put(RECORD_CHECK,false);
                mNameList.add(fileName);
                mPathList.add(path);
                mTitleList.add(createDate);
                mDurationList.add(showTimeCount(duration));
                mIdList.add(recordId);
                recordingFileCursor.moveToNext();
                mArrlist.add(map);

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
            mAdapter = new RecordCheckListAdapter(this, mArrlist);
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
        protected void onPostExecute(ArrayList<HashMap<String, Object>> hashMaps) {
            super.onPostExecute(hashMaps);
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

    class FileDeleteTask extends AsyncTask<Void,Object,Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            LogUtils.v(TAG,"FileDelete doInBackground begin");
            try {
                List<File> list = getSelectedFiles();
                int listSize = list.size();
                for (int i = 0; i < listSize; i++) {
                    File file = list.get(i);
                    if (null != file) {
                        LogUtils.v(TAG,"------------delete file path--->"+file.getAbsolutePath());
                    }
                    if (!file.delete()) {
                        LogUtils.v(TAG,"------------delete failed------------->"+list.get(i).getAbsolutePath());
                    }
                    if (!SoundRecorderUtils.deleteFileFromMediaDB(SoundRecordCheckListActivity.this,
                            file.getAbsolutePath())) {
                        LogUtils.v(TAG,"------------db delete failed------------->");
                        return false;
                    }
                }
                return true;
            }catch (Exception ex){
                LogUtils.v(TAG,"------------db delete failed------------->" +ex);
                return  false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            LogUtils.v(TAG,"FileDelete onPostExecute begin");
            setListData(null);
        }
    }
}

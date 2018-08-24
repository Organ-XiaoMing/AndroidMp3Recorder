package com.czt.mp3recorder.sample.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.czt.mp3recorder.MP3Recorder;
import com.czt.mp3recorder.sample.R;
import com.czt.mp3recorder.sample.SoundRecordService;
import com.czt.mp3recorder.sample.bean.Constant;
import com.czt.mp3recorder.sample.util.LogUtils;
import com.czt.mp3recorder.sample.view.HeadActionbar;
import com.czt.mp3recorder.util.LameUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ming.Xiao on 2018/8/21.
 */

public class SoundRecordActivity extends BaseActivity implements View.OnClickListener,SoundRecordService.RecorderListener{

    private static final String TAG = "SoundRecordActivity";

    private HeadActionbar mActionbar;
    private ImageView mStartRecord;
    private ImageView mSaveRecord;
    private ImageView mRecentRecord;
    private TextView mRecordTime;
    private TextView mRecordFile;
    private ImageView mBack;

    private boolean mIsHavePermission=true;

    private SoundRecordService mService;

    private static final int PERMISSION_RECORD_AUDIO = 1;
    private static final int PERMISSION_READ_STORAGE_LIST = 3;

    private int mCurrentState = SoundRecordService.STATE_IDLE;


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.v(TAG, "<onServiceConnected> Service connected");
            mService = ((SoundRecordService.SoundRecorderBinder) service).getService();
            mService.setListener(SoundRecordActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService.setListener(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_main);
        initActionbar();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsHavePermission = true;
        if(mService == null){
            if (!bindService(new Intent(SoundRecordActivity.this, SoundRecordService.class),
                    mServiceConnection, BIND_AUTO_CREATE)) {
                LogUtils.v(TAG, "<onResume> fail to bind service");
                finish();
                return;
            }
        }else{
            mService.setListener(SoundRecordActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mService = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.record_start:
                LogUtils.v(TAG,"startRecordingAsync mCurrentState " +mCurrentState);
                switch (mCurrentState){
                    case SoundRecordService.STATE_IDLE:
                        onClickRecordButton();
                        break;
                    case SoundRecordService.STATE_PAUSE_RECORDING:
                        LogUtils.v(TAG,"onresumeRecordingAsync");
                        mService.onresumeRecordingAsync();
                        break;
                    case SoundRecordService.STATE_RECORDING:
                        LogUtils.v(TAG,"pauseRecordingAsync");
                        mService.pauseRecordingAsync();
                        break;
                }
                break;
            case R.id.record_left:
                mService.saveRecordAsync();
                updateFileUI(null);
                break;
            case R.id.record_right:
                //mService.pauseRecordingAsync();
                launcerRecordList();
                break;
            case R.id.actionbar_back_btn:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onStateChanged(int stateCode) {
        Log.d(TAG, "onStateChanged: " +stateCode);
        mCurrentState = stateCode;
        updateUi(stateCode);
    }

    private void initActionbar(){
        mActionbar = (HeadActionbar) findViewById(R.id.action_bar);
        mActionbar.setTitle(R.string.record_title);
    }

    public void initView(){
        mStartRecord = (ImageView) findViewById(R.id.record_start);
        mStartRecord.setOnClickListener(this);
        mSaveRecord  = (ImageView) findViewById(R.id.record_left);
        mSaveRecord.setOnClickListener(this);
        mRecentRecord = (ImageView) findViewById(R.id.record_right);
        mRecentRecord.setOnClickListener(this);
        mRecordTime = (TextView) findViewById(R.id.record_time);
        mRecordFile = (TextView) findViewById(R.id.record_file);
        mBack = (ImageView) findViewById(R.id.actionbar_back_btn);
        mBack.setOnClickListener(this);
    }

    public void onClickRecordButton(){

        int recordAudioPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
        int readExtStorage = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeExtStorage = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> mPermissionStrings = new ArrayList<String>();
        boolean mRequest = false;
        LogUtils.v(TAG, "<onClickRecordButton1> " + recordAudioPermission + readExtStorage);
        if (readExtStorage != PackageManager.PERMISSION_GRANTED) {
            mPermissionStrings.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            mRequest = true;
        }
        if (writeExtStorage != PackageManager.PERMISSION_GRANTED) {
            mPermissionStrings.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            mRequest = true;
        }
        if (recordAudioPermission != PackageManager.PERMISSION_GRANTED) {
            mPermissionStrings.add(Manifest.permission.RECORD_AUDIO);
            mRequest = true;
        }
        if (mRequest == true) {
            String[] mPermissionList = new String[mPermissionStrings.size()];
            mPermissionList = mPermissionStrings.toArray(mPermissionList);
            requestPermissions(mPermissionList, PERMISSION_RECORD_AUDIO);
            mIsHavePermission=false;
            return;
        }

        LogUtils.v(TAG,"startRecordingAsync begin");
        String fileName = "REC"+System.currentTimeMillis()+".mp3";
        File file =   new File(Environment.getExternalStorageDirectory(),fileName);
        updateFileUI(file);
        mService.startRecordingAsync(file);

    }

    public void updateUi(int state){
        if(state == SoundRecordService.STATE_RECORDING){
            mStartRecord.setImageResource(R.drawable.record_pause);
        }
        if(state == SoundRecordService.STATE_PAUSE_RECORDING||state == SoundRecordService.STATE_IDLE){
            mStartRecord.setImageResource(R.drawable.record_nomal);
        }
    }

    public void updateFileUI(File file){
        if(file!=null){
            mRecordFile.setVisibility(View.VISIBLE);
            mRecordFile.setText(file.getName());
        }else{
            mRecordFile.setVisibility(View.GONE);
            mRecordFile.setText("");
        }
    }

    public void launcerRecordList(){
        Intent intent = new Intent(SoundRecordActivity.this,SoundRecordListActivity.class);
        startActivity(intent);
    }
}

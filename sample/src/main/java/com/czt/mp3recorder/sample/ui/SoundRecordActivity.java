package com.czt.mp3recorder.sample.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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


    private SoundRecordService mService;

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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.record_start:
                LogUtils.v(TAG,"startRecordingAsync mCurrentState " +mCurrentState);
                switch (mCurrentState){
                    case SoundRecordService.STATE_IDLE:
                        LogUtils.v(TAG,"startRecordingAsync begin");
                        File file =   new File(Environment.getExternalStorageDirectory(),Constant.temp);
                        updateFileUI(file);
                        mService.startRecordingAsync(file);
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

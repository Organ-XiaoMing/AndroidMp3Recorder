package com.czt.mp3recorder.sample;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import com.czt.mp3recorder.MP3Recorder;
import com.czt.mp3recorder.sample.bean.Constant;
import com.czt.mp3recorder.sample.util.LogUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by Ming.Xiao on 2018/8/21.
 */

public class SoundRecordService extends Service{

    public static final String TAG = "SoundRecordService";
    public static final int STATE_IDLE = 1;
    public static final int STATE_RECORDING = 2;
    public static final int STATE_PAUSE_RECORDING = 3;
    public static final int STATE_SAVE_RECORDING = 3;
    private int mCurrentState = STATE_IDLE;
    public static final String HANDLER_THREAD_NAME = "SoundRecorderServiceHandler";

    private SoundRecorderBinder mBinder = new SoundRecorderBinder();

    private MP3Recorder mMp3Record;
    SoundRecorderServiceHandler  mSoundRecorderServiceHandler;
    private HandlerThread mHandlerThread = null;

    private RecorderListener mListener;

    @Override
    public void onCreate() {
        super.onCreate();

        mMp3Record = new MP3Recorder(new File(Environment.getExternalStorageDirectory(),Constant.temp));

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

    public int getCurrentState() {
        return mCurrentState;
    }

    public void startRecordingAsync(){
        LogUtils.v(TAG, "<startRecordingAsync>");

        if(mCurrentState == STATE_RECORDING){
            return;
        }
        sendThreadHandlerMessage(SoundRecorderServiceHandler.START_REOCRD);
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

    public  class  SoundRecorderServiceHandler extends Handler{

        public SoundRecorderServiceHandler(Looper looper) {
            super(looper);
        }

        public static final int START_REOCRD = 0;
        public static final int PAUSE_REOCRD = 1;
        public static final int STOP_REOCRD = 2;
        public static final int SAVE_RECORD = 3;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case START_REOCRD:
                    record();
                    break;
                case PAUSE_REOCRD:
                    break;
                case STOP_REOCRD:
                    stoprecord();
                    break;
                case SAVE_RECORD:
                    stoprecord();
                    break;
            }
        }
    }

    public void record(){
        try {
            mMp3Record.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setState(STATE_RECORDING);
    }

    public void stoprecord(){
        mMp3Record.stop();
        setState(STATE_SAVE_RECORDING);
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

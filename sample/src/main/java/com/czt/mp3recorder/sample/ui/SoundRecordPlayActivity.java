package com.czt.mp3recorder.sample.ui;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.czt.mp3recorder.sample.Player;
import com.czt.mp3recorder.sample.R;
import com.czt.mp3recorder.sample.util.LogUtils;
import com.czt.mp3recorder.sample.view.HeadActionbar;

import org.w3c.dom.Text;

/**
 * Created by Ming.Xiao on 2018/8/23.
 */

public class SoundRecordPlayActivity extends Activity implements View.OnClickListener,Player.PlayerListener{

    public static final String TAG = "SoundRecordPlayActivity";

    private static final String DURATION = "duration";
    private static final String PATH = "path";
    private static final String FILE_NAME = "filename";
    public static final String HANDLER_THREAD_NAME = "SoundRecordPlayActivity";

    private TextView mPlayTimeTV;
    private TextView mCountDownTimeTV;
    private ImageView mPlayImageView;
    private HeadActionbar mActionbar;

    private  Bundle mBundle;

    private MediaPlayer mMediaPlayer = new MediaPlayer();

    private HandlerThread mHandlerThread = null;
    private SoundRecorderPlayHandler mSoundRecorderPlayHandler = null;

    private int mCurrentState = SoundRecorderPlayHandler.STATE_IDLE;

    private Player mPlayer = null;
    private String mCurrentFilePath = null;
    private String mFileName ="";
    private  String mDuration = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_play);
        initView();

        mPlayer = new Player(this);

        mHandlerThread = new HandlerThread(HANDLER_THREAD_NAME);
        mHandlerThread.start();
        mSoundRecorderPlayHandler = new SoundRecorderPlayHandler(mHandlerThread.getLooper());

    }

    @Override
    protected void onResume() {
        super.onResume();
        mBundle = getIntent().getExtras();
        mCurrentFilePath = mBundle.getString(PATH);
        mFileName = mBundle.getString(FILE_NAME);
        mDuration = mBundle.getString(DURATION);
        LogUtils.v(TAG,"the current file path is " +mCurrentFilePath);
        LogUtils.v(TAG,"the current mFileName is " +mFileName);
        LogUtils.v(TAG,"the current mDuration is " +mDuration);
        mActionbar.setTitle(mFileName);
        mCountDownTimeTV.setText(mDuration);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlaybackAsync();
    }

    private void initView(){
        mPlayTimeTV = (TextView) findViewById(R.id.play_time);
        mCountDownTimeTV = (TextView) findViewById(R.id.play_countdown_time);
        mPlayImageView = (ImageView) findViewById(R.id.play_pause);
        mPlayImageView.setOnClickListener(this);
        mActionbar = (HeadActionbar) findViewById(R.id.action_bar);
    }

    public void updateImageView(int stateCode){
        if(stateCode == SoundRecorderPlayHandler.STATE_PLAYING){
            mPlayImageView.setImageResource(R.drawable.pause);
        }else if(stateCode == SoundRecorderPlayHandler.STATE_PAUSE_PLAYING){
            mPlayImageView.setImageResource(R.drawable.play);
        }
    }

    @Override
    public void onClick(View v) {
            switch (v.getId()){
                case R.id.play_pause:
                    doPlay();
                    break;
            }
    }

    public void doPlay(){
        switch (mCurrentState){
            case SoundRecorderPlayHandler.STATE_IDLE:
                startPlaybackAsync();
                break;
            case SoundRecorderPlayHandler.STATE_PLAYING:
                pausePlaybackAsync();
                break;
            case SoundRecorderPlayHandler.STATE_PAUSE_PLAYING:
                goonPlaybackAsync();
                break;
        }
    }

    public boolean startPlayback() {
        LogUtils.v(TAG, "<startPlayback> in idle state, start play");
        mPlayer.setCurrentFilePath(mCurrentFilePath);
          return  mPlayer.startPlayback();
    }

    public boolean goonPlayback(int time) {
        LogUtils.v(TAG, "<goonPlayback> in pause play state, goon play");
        boolean res = false;
        return mPlayer.goonPlayback(time);
    }


    public boolean pausePlay() {
        LogUtils.v(TAG, "<pausePlay> in play state, pause play");
        return mPlayer.pausePlayback();
    }

    public boolean stopPlay() {
        LogUtils.v(TAG, "<stopPlay>");
        if ((SoundRecorderPlayHandler.STATE_PAUSE_PLAYING != mCurrentState) && (SoundRecorderPlayHandler.STATE_PLAYING != mCurrentState)) {
            LogUtils.v(TAG, "<stopPlay> not in play or pause play state, can't stop play");
            return false;
        }
        return mPlayer.stopPlayback(false);
    }

    public void goonPlaybackAsync() {
        LogUtils.v(TAG, "<goonPlaybackAsync>");
        sendThreadHandlerMessage(SoundRecorderPlayHandler.GOON_PLAY);
    }

    public void startPlaybackAsync() {
        LogUtils.v(TAG, "<startPlaybackAsync>");
        sendThreadHandlerMessage(SoundRecorderPlayHandler.START_PLAY);
    }

    public void pausePlaybackAsync() {
        LogUtils.v(TAG, "<pausePlaybackAsync>");
        sendThreadHandlerMessage(SoundRecorderPlayHandler.PAUSE_PLAY);
    }

    public void stopPlaybackAsync() {
        LogUtils.v(TAG, "<stopPlaybackAsync>");
        sendThreadHandlerMessage(SoundRecorderPlayHandler.STOP_PLAY);
    }

    private void sendThreadHandlerMessage(int what) {
        mSoundRecorderPlayHandler.removeCallbacks(mHandlerThread);
        mSoundRecorderPlayHandler.sendEmptyMessage(what);
    }

    @Override
    public void onError(Player player, int errorCode) {

    }

    @Override
    public void onStateChanged(Player player, int stateCode) {
        mCurrentState = stateCode;
        LogUtils.v(TAG,"onStateChanged " +stateCode);
        updateImageView(mCurrentState);
    }

    public class SoundRecorderPlayHandler extends Handler{

        public SoundRecorderPlayHandler(Looper looper){
            super(looper);
        }

        public static final int STATE_IDLE = -1;
        public static final int START_PLAY = 1;
        public static final int PAUSE_PLAY = 2;
        public static final int STOP_PLAY = 3;
        public static final int STATE_PLAYING = 4;
        public static final int STATE_PAUSE_PLAYING = 5;
        public static  final int GOON_PLAY = 6;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case START_PLAY:
                    startPlayback();
                    break;
                case PAUSE_PLAY:
                    pausePlay();
                    break;
                case GOON_PLAY:
                    goonPlayback(0);
                    break;
                case STOP_PLAY:
                    stopPlay();
                    break;
            }
        }
    }
}

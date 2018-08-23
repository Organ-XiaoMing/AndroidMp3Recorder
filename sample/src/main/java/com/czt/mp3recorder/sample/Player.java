package com.czt.mp3recorder.sample;

import android.media.MediaPlayer;
import android.util.Log;

import com.czt.mp3recorder.sample.ui.SoundRecordPlayActivity;
import com.czt.mp3recorder.sample.util.LogUtils;

import java.io.File;
import java.io.IOException;

/**
 * M: we split player from recorder, the player is only responsible for play back
 */
public class Player implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private static final String TAG = "SR/Player";
    private MediaPlayer mPlayer = null;
    private String mCurrentFilePath = null;
    private PlayerListener mListener = null;

    // M: the listener when error occurs and state changes
    public interface PlayerListener {
        // M: when error occurs, we will notify listener the error code
        void onError(Player player, int errorCode);

        // M: when state changes, we will notify listener the new state code
        void onStateChanged(Player player, int stateCode);
    }

    /**
     * M: Constructor of player, only PlayListener needed
     * @param listener the listener of player
     */
    public Player(PlayerListener listener) {
        mListener = listener;
    }

    @Override
    /**
     * M: the completion callback of MediaPlayer
     */
    public void onCompletion(MediaPlayer player) {
        LogUtils.v(TAG, "<onCompletion>");
        stopPlayback(false);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    /**
     * M: set the path of audio file which will play, used by
     * SoundRecorderService
     *
     * @param filePath
     */
    public void setCurrentFilePath(String filePath) {
        mCurrentFilePath = filePath;
    }

    /**
     * M: start play the audio file which is set in setCurrentFilePath
     * @return the result of play back, success or fail
     */
    public boolean startPlayback() {
        if (null == mCurrentFilePath) {
            return false;
        }
        LogUtils.v(TAG,"---------------mCurrentFilePath--->"+mCurrentFilePath);
        File file = new File(mCurrentFilePath);
        if (!file.exists()) {
            return false;
        }

        synchronized (this) {
            if (null == mPlayer) {
                mPlayer = new MediaPlayer();
                try {
                    mPlayer.setDataSource(mCurrentFilePath);
                    mPlayer.setOnCompletionListener(this);
                    mPlayer.prepare();
                    mPlayer.start();
                    LogUtils.v(TAG, "<startPlayback> The length of recording file is "
                            + mPlayer.getDuration());
                    setState(SoundRecordPlayActivity.SoundRecorderPlayHandler.STATE_PLAYING);
                } catch (IllegalStateException e) {
                    return handleException(e);
                } catch (IOException e) {
                    return handleException(e);
                }
            }
        }
        return true;
    }

    /**
     * M: Handle the Exception when call the function of MediaPlayer
     * @return false
     */
    public boolean handleException(Exception exception) {
        LogUtils.v(TAG, "<handleException>");
        exception.printStackTrace();
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        return false;
    }

    /**
     * M: pause play the audio file which is set in setCurrentFilePath
     */
    public boolean pausePlayback() {
        if (null == mPlayer) {
            return false;
        }
        try {
            mPlayer.pause();
            setState(SoundRecordPlayActivity.SoundRecorderPlayHandler.STATE_PAUSE_PLAYING);
        } catch (IllegalStateException e) {
            return handleException(e);
        }
        return true;
    }

    /**
     * M: goon play the audio file which is set in setCurrentFilePath
     */
    public boolean goonPlayback(int time) {
        if (null == mPlayer) {
            return false;
        }

        try {
            if (time != 0) {
                mPlayer.seekTo(time);
            }
            mPlayer.start();
            setState(SoundRecordPlayActivity.SoundRecorderPlayHandler.STATE_PLAYING);
        } catch (IllegalStateException e) {
            return handleException(e);
        }
        return true;
    }

    /**
     * M: stop play the audio file which is set in setCurrentFilePath
     */
    public boolean stopPlayback(boolean isClickToStop) {
        // we were not in playback
        synchronized (this) {
            if (null == mPlayer) {
                return false;
            }
            try {
                mPlayer.stop();
                setState(SoundRecordPlayActivity.SoundRecorderPlayHandler.STATE_IDLE);
            } catch (IllegalStateException e) {
                return handleException(e);
            }
            mPlayer.release();
            mPlayer = null;
        }
        return true;
    }

    /**
     * M: reset Player to initial state
     */
    public void reset() {
        synchronized (this) {
            if (null != mPlayer) {
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
            }
        }
        mCurrentFilePath = null;
    }

    /**
     * M: get the current position of audio which is playing
     * @return the current position in millseconds
     */
    public int getCurrentProgress() {
        if (null != mPlayer) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * M: get the duration of audio file
     * @return the duration in millseconds
     */
    public int getFileDuration() {
        if (null != mPlayer) {
            return mPlayer.getDuration();
        }
        return 0;
    }

    private void setState(int state) {
        mListener.onStateChanged(this, state);
    }
}

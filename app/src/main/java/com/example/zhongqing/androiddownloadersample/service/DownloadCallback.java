package com.example.zhongqing.androiddownloadersample.service;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by zhongqing on 23/7/17.
 */

public interface DownloadCallback {

    @Retention(SOURCE)
    @IntDef({STATE_START_DOWNLOADING,STATE_IDLE, STATE_ERROR,STATE_COMPLETE,STATE_PAUSE})
    public @interface DownloadState {
    }
    public int STATE_IDLE = 0;
    public int STATE_START_DOWNLOADING = 1;
    public int STATE_ERROR = 2;
    public int STATE_COMPLETE = 3;
    public int STATE_PAUSE = 4;




    void onReceiveUpdate(String identifier, double progress);

    void onStatusChanged(String identifier, @DownloadState int  state);
}

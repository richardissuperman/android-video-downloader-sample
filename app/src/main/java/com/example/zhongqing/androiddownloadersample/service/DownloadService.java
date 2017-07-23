package com.example.zhongqing.androiddownloadersample.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.zhongqing.androiddownloadersample.utils.Utils;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by zhongqing on 23/7/17.
 */

public class DownloadService extends Service {



    private DownloadBinder downloadBinder;

    public class DownloadBinder extends Binder{

        private ExecutorService executorService;

        private DownloadCallback downloadCallback;

        private HashMap<String,Future> futureHashMap = new HashMap();

        public DownloadBinder(){
            executorService = Executors.newFixedThreadPool(Utils.getNumberOfCores(),new DownLoadThreadFactory(DownloadService.this));
        }

        public void registerDownLoadCallback(DownloadCallback callback){
            this.downloadCallback = callback;
        }

        public void onDestroy(){
            downloadCallback = new DownloadCallback() {
                @Override
                public void onReceiveUpdate(String identifier, double progress) {
                    Log.e("Richard","keep downloading even activity is gone " + progress);
                }

                @Override
                public void onStatusChanged(String identifier, @DownloadState int state) {

                }
            };
        }

        public void call(){
        }

        public void tryDownLoad(String url,String savePath){
            Future future = executorService.submit(DownLoadThreadFactory.getDownLoadRunnable(url,savePath,downloadCallback));
            futureHashMap.put(Utils.getVideoIdentifier(url),future);
        }


        public void tryPauseDownLoad(String url){
            String id = Utils.getVideoIdentifier(url);
            if(futureHashMap.containsKey(id)){
                futureHashMap.get(id).cancel(true);
                downloadCallback.onStatusChanged(id,DownloadCallback.STATE_PAUSE);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(downloadBinder == null){
            downloadBinder = new DownloadBinder();
        }
        return downloadBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if(downloadBinder != null){
            downloadBinder.onDestroy();
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

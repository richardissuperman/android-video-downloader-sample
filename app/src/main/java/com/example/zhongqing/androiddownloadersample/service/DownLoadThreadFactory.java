package com.example.zhongqing.androiddownloadersample.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.example.zhongqing.androiddownloadersample.utils.Utils;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ThreadFactory;

/**
 * Created by zhongqing on 23/7/17.
 */

public class DownLoadThreadFactory implements ThreadFactory {


    private Context context;

    public DownLoadThreadFactory(Context context){
        this.context = context;
    }


    public class DownLoadThread extends Thread{
        private SharedPreferences preferences;

        public DownLoadThread(Context context,Runnable runnable){
            super(runnable);
            this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        public void markProgress(String id, long progress){
            preferences.edit().putLong(id,progress).apply();
        }
    }

    @Override
    public Thread newThread(@NonNull Runnable r) {
        return new DownLoadThread(context,r);
    }

    public static Runnable getDownLoadRunnable(@NonNull final String downloadUrl, final String path, final DownloadCallback callback) {
        return new Runnable() {
            @Override
            public void run() {
                try {

                    String identifier = Utils.getVideoIdentifier(downloadUrl);

                    callback.onStatusChanged(identifier,DownloadCallback.STATE_START_DOWNLOADING);

                    URL url = new URL(downloadUrl);
                    HttpURLConnection preCon = (HttpURLConnection) url
                            .openConnection();
                    preCon.setRequestMethod("GET");
                    preCon.setReadTimeout(3000);

                    long size = preCon.getContentLengthLong();
                    long startPos = 0;
                    long endPos = preCon.getContentLength();

                    Thread thread = Thread.currentThread();


                    String fileName = path+File.separator+identifier;

                    File file = new File(fileName);
                    if (file.exists() && file.length() > 0) {
                       startPos = file.length();
                    }

                    if(startPos >= endPos){
                        callback.onStatusChanged(identifier,DownloadCallback.STATE_COMPLETE);
                        //this file has been downloaded fully already;
                        return;
                    }




                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.setRequestMethod("GET");
                    conn.setReadTimeout(3000);
                    conn.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
                    RandomAccessFile raf = new RandomAccessFile(fileName, "rwd");

                    if (startPos > 0) {
                        raf.seek(startPos);

                    }

                    InputStream is = conn.getInputStream();
                    byte[] b = new byte[1024 * 1024 * 10];
                    int len = -1;
                    long newPos = startPos;
                    while ((len = is.read(b)) != -1) {
                        raf.write(b, 0, len);
                        float percent = (float) ((newPos+len))/size;
                        callback.onReceiveUpdate(identifier,percent*100);
                        newPos = newPos + len;

                        if(thread instanceof DownLoadThread){
                            ((DownLoadThread) thread).markProgress(identifier,(long)(percent*100));
                        }
                    }
                    is.close();
                    raf.close();
                    callback.onStatusChanged(identifier,DownloadCallback.STATE_COMPLETE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}

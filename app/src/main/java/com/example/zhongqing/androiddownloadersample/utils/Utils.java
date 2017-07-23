package com.example.zhongqing.androiddownloadersample.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.example.zhongqing.androiddownloadersample.bean.Video;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import rx.Observable;

/**
 * Created by zhongqing on 23/7/17.
 */

public class Utils {



    public static String getDownLoadPath(Context context){
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String getFullVideoFilePath(Context context,Video video){
        return getDownLoadPath(context)+File.separator+getVideoIdentifier(video.getVideoUrl());
    }



    public static Observable<Void> getVideoCheckObservable(final Video video, final Context context){
        return Observable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                File file = new File(getFullVideoFilePath(context,video));
                if ( !file.exists() || file.length() <= 0) {
                    pref.edit().putLong(getVideoIdentifier(video.getVideoUrl()),0).apply();
                }
                return null;
            }
        });
    }

    public static String getVideoIdentifier(@NonNull  String url){
        return Integer.toHexString(url.hashCode())+".mp4";
    }

    public static int getNumberOfCores() {
        if(Build.VERSION.SDK_INT >= 17) {
            return Runtime.getRuntime().availableProcessors();
        }
        else {
            return getNumCoresOldPhones();
        }
    }

    private static int getNumCoresOldPhones() {
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                if(Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new CpuFilter());
            return files.length;
        } catch(Exception e) {
            return 1;
        }
    }

}

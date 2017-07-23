package com.example.zhongqing.androiddownloadersample.bean;

import com.example.zhongqing.androiddownloadersample.service.DownloadCallback;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zhongqing on 23/7/17.
 */

public class Video {
    private String videoUrl;
    private String videoPoster;
    private int downloadProgress;
    private @DownloadCallback.DownloadState int state;
    private Video(String url,String poster){
        this.videoUrl = url;
        this.videoPoster = poster;
    }
    public static Video readFromJson(JSONObject object) throws JSONException{
        String poster = null;
        String url = null;
        if(object.has("poster")){
            poster = object.getString("poster");
        }

        if(object.has("url")){
            url = object.getString("url");
        }
        return new Video(url,poster);
    }


    public void setDownloadProgress(int progress){
        this.downloadProgress = progress;
    }

    public void setState(@DownloadCallback.DownloadState int state){
        this.state = state;
    }

    public @DownloadCallback.DownloadState int getState(){
        return this.state;
    }


    public int getDownloadProgress(){
        return this.downloadProgress;
    }
    public String getVideoPoster(){
        return videoPoster;
    }

    public String getVideoUrl(){
        return videoUrl;
    }
}

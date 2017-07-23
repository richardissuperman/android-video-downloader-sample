package com.example.zhongqing.androiddownloadersample.pref;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.example.zhongqing.androiddownloadersample.bean.Video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhongqing on 23/7/17.
 */

public class PrefHelper {

    public static final String VIDEO_DATA = "video_data";


    public static void trySavePrefs(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (TextUtils.isEmpty(sharedPreferences.getString(VIDEO_DATA, ""))) {
            sharedPreferences.edit()
                    .putString(VIDEO_DATA, getDefaultData(context))
                    .apply();
        }
    }


    public static List<Video> getDefaultVideos(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            JSONObject jsonObject = new JSONObject(sharedPreferences.getString(VIDEO_DATA, ""));
            JSONArray array = jsonObject.getJSONObject("video_list").getJSONArray("list");
            ArrayList<Video> result = new ArrayList<>();
            for(int i= 0; i< array.length() ;i++){
                result.add( Video.readFromJson(array.getJSONObject(i)) );
            }
            return result;
        }
        catch ( JSONException e){
            e.printStackTrace();
        }

        return null;
    }


    public static String getDefaultData(Context context) {
        return getDefaultResponse(context, "default_settings.json");
    }


    public static String getDefaultResponse(Context context, String filename) {
        try {
            //InputStream is= context.getClass().getResourceAsStream("/assets/" + filename);
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String content = new String(buffer);
            return content;
        } catch (Exception e) {
            return "";
        }
    }

}

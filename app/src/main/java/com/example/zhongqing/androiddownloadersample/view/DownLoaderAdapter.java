package com.example.zhongqing.androiddownloadersample.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.zhongqing.androiddownloadersample.R;
import com.example.zhongqing.androiddownloadersample.bean.Video;
import com.example.zhongqing.androiddownloadersample.service.DownloadCallback.DownloadState;
import com.example.zhongqing.androiddownloadersample.service.DownloadCallback;
import com.example.zhongqing.androiddownloadersample.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by zhongqing on 23/7/17.
 */

public class DownLoaderAdapter extends RecyclerView.Adapter<DownLoaderAdapter.DownLoadViewHolder> implements DownloadCallback {

    private IDownLoadingCommand command;
    private List<Video> data;
    private List<Integer> states;
    private Context context;
    private HashSet<String> startedIndicator = new HashSet<>();
    private SharedPreferences preferences;


    public DownLoaderAdapter(@NonNull  IDownLoadingCommand downLoadingCommand, @NonNull List<Video> data){
        this.context = downLoadingCommand.getDownLoadContext();
        this.command = downLoadingCommand;
        this.data = data;
        states = new ArrayList<>();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        for(Video video : data){
            states.add(STATE_IDLE);
        }
    }


    @Override
    public void onReceiveUpdate(String identifier, double progress) {
        for(int i = 0 ;i < data.size() ;i++){
            Video video = data.get(i);
            final int positon = i;
            if(Utils.getVideoIdentifier(video.getVideoUrl()).equals(identifier)){
                video.setDownloadProgress((int)progress);
                new Handler(Looper.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                notifyItemChanged(positon);
                            }
                        });
                break;
            }
        }
    }


    @Override
    public void onStatusChanged(String identifier, @DownloadState int  state) {
        for(int i = 0 ;i < data.size() ;i++){
            Video video = data.get(i);
            final int positon = i;
            if(Utils.getVideoIdentifier(video.getVideoUrl()).equals(identifier)){
                video.setState(state);
                new Handler(Looper.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                notifyItemChanged(positon);
                            }
                        });
                break;
            }
        }
    }

    @Override
    public void onBindViewHolder(final DownLoadViewHolder holder, final int position) {
        Video video = data.get(position);
        Glide.with(context)
                .load(video.getVideoPoster())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_placeholder)
                .into(holder.mainImageView);

        final String videoId = Utils.getVideoIdentifier(video.getVideoUrl());
        holder.downLoadProgressBar.setProgress(video.getDownloadProgress());
        holder.startPauseIcon.setOnClickListener( null );
        if(!startedIndicator.contains(videoId)){
            int progress = (int)preferences.getLong(videoId,0);
            holder.downLoadProgressBar.setProgress(progress);
            holder.startPauseIcon.setImageResource(R.drawable.play);
            if(progress > 0 && progress < 100) {
                holder.progressContainer.setVisibility(View.VISIBLE);
            }
            else if(progress == 0){
                handleStateChange(video,position,STATE_IDLE,holder);
            }
            else{
                handleStateChange(video,position,STATE_COMPLETE,holder);

            }
            holder.startPauseIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.progressContainer.setVisibility(View.VISIBLE);
                    holder.downLoadProgressBar.setProgress(5);
                    startedIndicator.add(videoId);
                    //start
                    command.startDownload(data.get(position).getVideoUrl());
                }
            });
            return;
        }


        handleStateChange(video,position,video.getState(),holder);




    }


    private void handleStateChange(Video video,final int position, @DownloadState int state, final DownLoadViewHolder holder){

        final String videoId = Utils.getVideoIdentifier(video.getVideoUrl());
        switch ( state ){
            case STATE_IDLE:
                holder.startPauseIcon.setVisibility(View.VISIBLE);
                holder.startPauseIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.progressContainer.setVisibility(View.VISIBLE);
                        holder.downLoadProgressBar.setProgress(5);
                        startedIndicator.add(videoId);
                        //start
                        command.startDownload(data.get(position).getVideoUrl());
                    }
                });
                holder.startPauseIcon.setImageResource(R.drawable.play);
                holder.progressContainer.setVisibility(View.GONE);
                break;
            case STATE_START_DOWNLOADING:
                holder.startPauseIcon.setVisibility(View.VISIBLE);
                holder.startPauseIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //pause
                        command.pauseDownload(data.get(position).getVideoUrl());
                    }
                });
                holder.startPauseIcon.setImageResource(R.drawable.pause);
                holder.progressContainer.setVisibility(View.VISIBLE);
                break;

            case STATE_PAUSE:
                holder.startPauseIcon.setVisibility(View.VISIBLE);
                holder.startPauseIcon.setImageResource(R.drawable.play);
                holder.startPauseIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //resume
                        command.startDownload(data.get(position).getVideoUrl());
                    }
                });
                holder.progressContainer.setVisibility(View.VISIBLE);
                break;
            case STATE_COMPLETE:
                holder.startPauseIcon.setVisibility(View.GONE);
                holder.startPauseIcon.setOnClickListener(null);
                holder.progressContainer.setVisibility(View.VISIBLE);
                Toast.makeText(context,video.getVideoUrl() + "  has completed downloading!",Toast.LENGTH_SHORT).show();
                break;
        }
    }


    @Override
    public DownLoadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.row_vieo, parent, false);
        return new DownLoadViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public static interface IDownLoadingCommand{
        void startDownload(String url);
        void pauseDownload(String url);
        Context getDownLoadContext();
    }

    public class DownLoadViewHolder extends RecyclerView.ViewHolder{
        private ImageView startPauseIcon;
        private View progressContainer;
        private DownLoadProgressBar downLoadProgressBar;
        private ImageView mainImageView;
        public DownLoadViewHolder(View root){
            super(root);

            startPauseIcon = (ImageView) root.findViewById(R.id.start_pause_button);
            progressContainer = root.findViewById(R.id.progress_container);
            downLoadProgressBar = (DownLoadProgressBar) root.findViewById(R.id.download_progressbar);
            mainImageView = (ImageView) root.findViewById(R.id.download_imageview);
        }
    }
}

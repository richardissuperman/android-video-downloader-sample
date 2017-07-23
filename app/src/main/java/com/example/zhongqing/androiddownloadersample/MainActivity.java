package com.example.zhongqing.androiddownloadersample;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.zhongqing.androiddownloadersample.bean.Video;
import com.example.zhongqing.androiddownloadersample.pref.PrefHelper;
import com.example.zhongqing.androiddownloadersample.service.DownloadCallback;
import com.example.zhongqing.androiddownloadersample.service.DownloadService;
import com.example.zhongqing.androiddownloadersample.utils.Utils;
import com.example.zhongqing.androiddownloadersample.view.DownLoaderAdapter;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements DownloadCallback ,DownLoaderAdapter.IDownLoadingCommand{


    private DownloadService.DownloadBinder myBinder;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 101;
    private DownLoaderAdapter adapter;
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PrefHelper.trySavePrefs(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        doCheck();
    }

    @Override
    public void onReceiveUpdate(String identifier, double progress) {
        Log.e("RICHARD","+++++   "+progress);
        adapter.onReceiveUpdate(identifier,progress);
    }


    @Override
    public void onStatusChanged(String identifier, @DownloadState int  state) {
        adapter.onStatusChanged(identifier,state);
    }

    @Override
    public Context getDownLoadContext() {
        return this;
    }

    @Override
    public void startDownload(String url) {
        if(conn != null && myBinder != null){
            myBinder.tryDownLoad(url, Utils.getDownLoadPath(this));
        }
        else {
            Toast.makeText(this,"Sorry, there's some error, please try again later",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void pauseDownload(String url) {
        if(conn != null && myBinder != null){
            myBinder.tryPauseDownLoad(url);
        }
        else {
            Toast.makeText(this,"Sorry, there's some error, please try again later",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myBinder != null) {
            unbindService(conn);
        }
        myBinder = null;
        if(subscription != null && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == MY_PERMISSIONS_REQUEST_WRITE_STORAGE){
            if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_STORAGE && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initViews();
                    startService();
                    startBindService();
                } else {
                    Toast.makeText(this,"We need this permission to download man!",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void initViews(){
        View progressBarContainer = findViewById(R.id.progressbar_container);
        progressBarContainer.setVisibility(View.GONE);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        adapter = new DownLoaderAdapter(this,PrefHelper.getDefaultVideos(this));
        recyclerView.setAdapter(adapter);
    }


    private void doCheck() {
        subscription = Observable.from(PrefHelper.getDefaultVideos(this))
                .subscribeOn(Schedulers.computation())
                .flatMap(new Func1<Video, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(Video video) {
                        return Utils.getVideoCheckObservable(video,MainActivity.this);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        askForPermission();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                });


    }


    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            myBinder = (DownloadService.DownloadBinder)binder;
            myBinder.call();
            myBinder.registerDownLoadCallback(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    private void startService(){
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("from", "ActivityA");
        startService(intent);
    }

    private void startBindService(){
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("from", "ActivityA");
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    private void askForPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
            }
        }
        else{
            initViews();
            startService();
            startBindService();
        }
    }
}

# android-video-downloader-sample



## 一个简易的安卓端下载文件的小程序。




![](https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTmt8kJnUaxzreAiIxcWMqebPx_VwpLk13ZbTkPu-jBf_7x0BYRl47wRw)


****************

### 基本原理
>
>1. 使用了Java的线程池[ExecutorService](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html)和[Future](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Future.html)来控制简单的开始下载和暂停。

>2. 用Service来host线程池，通过bindService()获取Service的Binder句柄，使得Activity组件可以对Service的线程池进行控制。


>3. 下载的程序片段使用HttpURLConnection进行下载，Java的基本文件流读写进行写文件。

*****************

### 组件通信

组建通信方面使用简单的Java面向对象，面向接口编程的概念，通过接口/回调类在Activity,Service，和列表的Adapter之间进行通信,没有使用类似EventBus的library。

附上一个简单的结构图(偷懒不想画UML,逃。。。)

![](https://github.com/richardissuperman/android-video-downloader-sample/blob/master/images/Screen%20Shot%202017-07-23%20at%208.10.50%20PM.png?raw=true)


 
 ### 启动流程
 
 对于意外终止、或者人为终止的情况，重启程序重新加载进度的功能使用[SharedPreference](https://developer.android.com/reference/android/content/SharedPreferences.html)进行处理。
 
 
 ![](https://github.com/richardissuperman/android-video-downloader-sample/blob/master/images/Screen%20Shot%202017-07-23%20at%208.18.38%20PM.png?raw=true)
 
 
 另外使用了RxJava进行启动检查(因为涉及到文件检索所以是耗时工作)
 
 ```java
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
```
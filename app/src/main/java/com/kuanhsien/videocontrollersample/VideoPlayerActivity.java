package com.kuanhsien.videocontrollersample;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.Console;
import java.io.IOException;


public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, VideoControllerView.MediaPlayerControl {

    private static final String TAG = "KEN_MOVIE";
    
    private String mVideoUrl = "https://s3-ap-northeast-1.amazonaws.com/mid-exam/Video/taeyeon.mp4";
    

    SurfaceView videoSurface;
    MediaPlayer player;
    VideoControllerView controller;
    ConstraintLayout constraintLayoutVideoContainer;
    int mScreenWidth;
    int mScreenHeight;

    Boolean mIsFullScreen;
    Boolean mIsMute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        constraintLayoutVideoContainer = findViewById(R.id.video_container);
        mScreenWidth = constraintLayoutVideoContainer.getWidth();
        mScreenHeight = constraintLayoutVideoContainer.getHeight();

        Log.d("KEN_MOVIE", "onCreate" );


        // initialization
        // [TODO] 偵測起始方向
        mIsFullScreen = false;
        mIsMute = false;
        videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
        SurfaceHolder videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);  //related to the implements SurfaceHolder.Callback

        player = new MediaPlayer();
        controller = new VideoControllerView(this);

        try {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            player.setDataSource(this, Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"));
            // [TODO] Change the video url
            player.setDataSource(this, Uri.parse(mVideoUrl));
            player.setOnPreparedListener(this); // relates to the MediaPlayer.OnPreparedListener reference in the class definition

//            player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
//                public void onBufferingUpdate(MediaPlayer mp, int percent)
//                {
//                    double ratio = percent / 100.0;
//                    int bufferingLevel = (int)(mp.getDuration() * ratio);
//
//                    controller.mProgress.setSecondaryProgress(bufferingLevel);
//                }
//
//            });

            // [TODO]
            // onComplete 的時候換回 Play 的按鈕， seek to position 0
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    try {
                        Log.d(TAG, "onCompletion");
                        player.seekTo(0);
                        controller.updatePausePlay();
                    }
                    catch (Exception e) {
                        Log.e(TAG, e.getMessage(),e);
                    }
                }
            });
            
            

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // show the controller when touch the screen
        controller.show();
        return false;
    }


    // Implement SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        player.setDisplay(holder);
        player.prepareAsync();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    // End SurfaceHolder.Callback


    // Links the media controller with the media player and places the controller in the same container as the player
    // Implement MediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(MediaPlayer mp) {
        controller.setMediaPlayer(this);
        controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));

        // set the video size first time
        mScreenWidth = constraintLayoutVideoContainer.getWidth();
        mScreenHeight = constraintLayoutVideoContainer.getHeight();
        changeVideoSize(mScreenWidth, mScreenHeight);
        player.start();
    }
    // End MediaPlayer.OnPreparedListener



    // Implement VideoMediaController.MediaPlayerControl
    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void seekTo(int i) {
        player.seekTo(i);
    }

    @Override
    public void start() {
        player.start();
    }

    @Override
    public boolean isFullScreen() {
        return mIsFullScreen;
    }

    @Override
    public boolean isMute() {
        return mIsMute;
    }

    @Override
    public void setVolume(float l, float r) {
        player.setVolume(l, r);

        if (isMute()) {
            setIsMute(false);
        } else {
            setIsMute(true);
        }

    }

    @Override
    public void toggleFullScreen() {

        // if current mode is full screen -> change to normal screen
        if (isFullScreen()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }
    // End VideoMediaController.MediaPlayerControl


    public void setFullScreen(Boolean fullScreen) {

        mIsFullScreen = fullScreen;
    }

    public void setIsMute(Boolean isMute) {

        mIsMute = isMute;
    }






    // 可能透過強制旋轉、或是手機旋轉進來
    // capture screen orientation change
    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        // 1. change flag (mIsFulLScreen)
        // 2. [TODO] change media controller default timeout
        // 3. [TODO] change fullscreen button
        // 4. 呼叫 changeVideoSize()

        int screenWidth;
        int screenHeight;

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
            setFullScreen(true);
            screenWidth = mScreenHeight;
            screenHeight = mScreenWidth;
            controller.setControlTimeout(controller.MODE_FULLSCREEN);


        } else {
            // (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
            setFullScreen(false);
            screenWidth = mScreenWidth;
            screenHeight = mScreenHeight;
            controller.setControlTimeout(controller.MODE_NORMAL);
        }

        Log.d("KEN_MOVIE", "onConfigurationChanged: " + newConfig);


//        setContentView(R.layout.activity_video_player);
//        videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
        super.onConfigurationChanged(newConfig);

        changeVideoSize(screenWidth, screenHeight);


        // 平放時可以透過按畫面上的按鈕來做到轉動，拿起時必須重新偵測現在的手機方向，並轉回正確的方向
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }



//    /**
//     * 改变视频的显示大小，全屏，窗口，内容
//     */
//    public void changeVideoSize() {
//
////        // 取得螢幕解析度
////        DisplayMetrics dm = new DisplayMetrics();
////        getWindowManager().getDefaultDisplay().getMetrics(dm);
////
////        int vWidth = dm.widthPixels;
////        int vHeight = dm.heightPixels;
//
//
//        // 获取视频的宽度和高度
//        int width = player.getVideoWidth();
//        int height = player.getVideoHeight();
//
//        ConstraintLayout constraintLayoutVideoContainer = findViewById(R.id.video_container);
//        int screenWidth = constraintLayoutVideoContainer.getMaxWidth();
//        int screenHeight = constraintLayoutVideoContainer.getMaxHeight();
//
//
//        // 如果按钮文字为窗口则设置为窗口模式
//        if (!isFullScreen()) {
//            /*
//             * 如果为全屏模式则改为适应内容的，前提是视频宽高小于屏幕宽高，如果大于宽高 我们要做缩放
//             * 如果视频的宽高度有一方不满足我们就要进行缩放. 如果视频的大小都满足就直接设置并居中显示。
//             */
//            if (width > screenWidth || height > screenHeight) {
//                // 计算出宽高的倍数
//                float vWidth = (float) width / (float) screenWidth;
//                float vHeight = (float) height / (float) screenHeight;
//                // 获取最大的倍数值，按大数值进行缩放
//                float max = Math.max(vWidth, vHeight);
//                // 计算出缩放大小,取接近的正值
//                width = (int) Math.ceil((float) width / max);
//                height = (int) Math.ceil((float) height / max);
//            }
//            // 设置SurfaceView的大小并居中显示
//            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width,
//                    height);
//            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//            videoSurface.setLayoutParams(layoutParams);
//
//        } else if (isFullScreen()) {
//
//            // 设置全屏
//            // 设置SurfaceView的大小并居中显示
//            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidth,
//                    screenHeight);
//            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//            videoSurface.setLayoutParams(layoutParams);
//
//        }
//    }


    /**
     * 改变视频的显示大小，全屏，窗口，内容
     */
    public void changeVideoSize(int screenWidth, int screenHeight) {

        // 获取视频的宽度和高度
        int width = player.getVideoWidth();
        int height = player.getVideoHeight();

        float vWidth = (float) width / (float) screenWidth;
        float vHeight = (float) height / (float) screenHeight;

        RelativeLayout.LayoutParams layoutParams;

        if (isFullScreen()) {

            // 设置全屏
            // 设置SurfaceView的大小并居中显示
            layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
        } else {
            // 获取最大的倍数值，按大数值进行缩放
            float max = Math.max(vWidth, vHeight);
            // 计算出缩放大小,取接近的正值
            width = (int) Math.ceil((float) width / max);
            height = (int) Math.ceil((float) height / max);

            layoutParams = new RelativeLayout.LayoutParams(width,
                    height);
        }

        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        videoSurface.setLayoutParams(layoutParams);

    }

}

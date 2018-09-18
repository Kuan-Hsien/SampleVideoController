package com.kuanhsien.videocontrollersample;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;


public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, VideoControllerView.MediaPlayerControl {

    private static final String TAG = "KEN_MOVIE";

    // width 較大的影片
    //    private String mVideoUrl = "https://s3-ap-northeast-1.amazonaws.com/mid-exam/Video/protraitVideo.mp4";

    // height 較大的影片
    private String mVideoUrl = "https://s3-ap-northeast-1.amazonaws.com/mid-exam/Video/taeyeon.mp4";
    

    private SurfaceView mVideoSurface;
    private MediaPlayer mPlayer;
    private VideoControllerView mController;
    private ConstraintLayout mConstraintLayoutVideoContainer;

    private int mScreenShortEdge;   // 手機短邊：直立時的寬
    private int mScreenLongEdge;  // 手機長邊：直立時的高

    private int mSecondaryProgress;


    Boolean mIsFullScreen;
    Boolean mIsMute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        setStatusBar();

        mConstraintLayoutVideoContainer = findViewById(R.id.video_container);

        Log.d("KEN_MOVIE", "onCreate" );


        // initialization
        // [TODO] 偵測起始方向
        mIsFullScreen = false;
        mIsMute = false;
        mVideoSurface = (SurfaceView) findViewById(R.id.videoSurface);
        SurfaceHolder videoHolder = mVideoSurface.getHolder();
        videoHolder.addCallback(this);  //related to the implements SurfaceHolder.Callback

        mPlayer = new MediaPlayer();
        mController = new VideoControllerView(this);

        try {
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mPlayer.setDataSource(this, Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"));
            // [TODO] Change the video url
            mPlayer.setDataSource(this, Uri.parse(mVideoUrl));
            mPlayer.setOnPreparedListener(this); // relates to the MediaPlayer.OnPreparedListener reference in the class definition



            // onComplete 的時候換回 Play 的按鈕， seek to position 0
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    try {
                        Log.d(TAG, "onCompletion");
                        mPlayer.seekTo(0);
                        mController.updatePausePlay();
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

        // show the mController when touch the screen
        mController.show();
        return false;
    }


    // Implement SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mPlayer.setDisplay(holder);
        mPlayer.prepareAsync();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    // End SurfaceHolder.Callback


    // Links the media mController with the media mPlayer and places the mController in the same container as the mPlayer
    // Implement MediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(MediaPlayer mp) {
        mController.setMediaPlayer(this);
        mController.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));

        Log.d(TAG, "onPrepared:");
        Log.d(TAG, "mConstraintLayoutVideoContainer.getHeight() = " + mConstraintLayoutVideoContainer.getHeight());
        Log.d(TAG, "mConstraintLayoutVideoContainer.getWidth() = " + mConstraintLayoutVideoContainer.getWidth());
        Log.d(TAG, "getStatusBarHeight = " + getStatusBarHeight());

        // set the video size first time
        if (mConstraintLayoutVideoContainer.getWidth() > mConstraintLayoutVideoContainer.getHeight()) {

            //一開始為橫向，改為直向長寬
            mScreenShortEdge = mConstraintLayoutVideoContainer.getHeight() + getStatusBarHeight();
            mScreenLongEdge = mConstraintLayoutVideoContainer.getWidth();

            setFullScreen(true);
            changeVideoSize(mScreenLongEdge, mScreenShortEdge);

        } else {

            //一開始為直向
            mScreenShortEdge = mConstraintLayoutVideoContainer.getWidth();
            mScreenLongEdge = mConstraintLayoutVideoContainer.getHeight() + getStatusBarHeight();

            setFullScreen(false);
            changeVideoSize(mScreenShortEdge, mScreenShortEdge);
        }


        // set video download status (buffer)
        mPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            public void onBufferingUpdate(MediaPlayer mp, int percent)
            {
                mSecondaryProgress = (int)(mp.getDuration() * percent / 100.0);

                Log.d(TAG, "percent = " + percent);
                Log.d(TAG, "getDuration() = " + getDuration());
                Log.d(TAG, "mSecondaryProgress = " + mSecondaryProgress);
//                mController.mProgress.setSecondaryProgress(bufferingLevel);
            }

        });


        mPlayer.start();
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
        return mPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    @Override
    public void pause() {
        mPlayer.pause();
    }

    @Override
    public void seekTo(int i) {
        mPlayer.seekTo(i);
    }

    @Override
    public void start() {
        mPlayer.start();
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
        mPlayer.setVolume(l, r);

        if (isMute()) {
            setIsMute(false);
        } else {
            setIsMute(true);
        }

    }

    @Override
    public void toggleFullScreen() {

        if (isFullScreen()) {
            // if current mode is full screen -> change to normal screen
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        } else {
            // if current mode is normal -> change to full screen
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }
    // End VideoMediaController.MediaPlayerControl


    // Setter
    public void setFullScreen(Boolean fullScreen) {

        mIsFullScreen = fullScreen;
    }

    public void setIsMute(Boolean isMute) {

        mIsMute = isMute;
    }

    @Override
    public int getSecondaryProgress() {
        return mSecondaryProgress;
    }

    // 可能透過強制旋轉、或是手機旋轉進來
    // 手機偵測到轉換方向會進來 onConfigurationChanged，這時可做進一步的設定
    // capture screen orientation change
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.d("KEN_MOVIE", "onConfigurationChanged: " + newConfig);

        // 1. change flag (mIsFulLScreen)
        // 2. [TODO] change media mController default timeout
        // 3. [TODO] change fullscreen button
        // 4. 呼叫 changeVideoSize()

        // Checks the orientation of the screen
        // newConfig 表示轉完之後的方向
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();

            setFullScreen(true);
            mController.setControlTimeout(mController.MODE_FULLSCREEN);
            mController.updateFullScreen(); // change icon
            changeVideoSize(mScreenLongEdge, mScreenShortEdge); // 轉完是水平的話，影片需要對齊的橫向為手機的長邊

        } else {
            // (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();

            setFullScreen(false);
            mController.setControlTimeout(mController.MODE_NORMAL);
            mController.updateFullScreen(); // change icon
            changeVideoSize(mScreenShortEdge, mScreenShortEdge);
        }

        // 平放時可以透過按畫面上的按鈕來做到轉動，拿起時必須重新偵測現在的手機方向，並轉回正確的方向
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }


    /**
     * 配合手機方向或是全螢幕/正常播放模式，修改 video 的大小
     */
    public void changeVideoSize(int screenWidth, int screenHeight) {

        // 抓出 video 的高度和寬度
        int width = mPlayer.getVideoWidth();
        int height = mPlayer.getVideoHeight();

        float vWidth = (float) width / (float) screenWidth;
        float vHeight = (float) height / (float) screenHeight;

        RelativeLayout.LayoutParams layoutParams;

        if (vWidth > vHeight) {
            layoutParams = new RelativeLayout.LayoutParams(screenWidth, (int) (height/vWidth));
        } else {
            layoutParams = new RelativeLayout.LayoutParams((int) (width/vHeight), screenHeight);
        }

//        if (isFullScreen()) {
//
//            float max = Math.max(vWidth, vHeight);
////             // 計算出縮放大小，取接近的正值
//            width = (int) Math.ceil((float) width / max);
//            height = (int) Math.ceil((float) height / max);
//
//            layoutParams = new RelativeLayout.LayoutParams(width, height);
//
//            // 设置全屏
//            // 设置SurfaceView的大小并居中显示
////            layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
////                    RelativeLayout.LayoutParams.MATCH_PARENT);
//        } else {
//            // 获取最大的倍数值，按大数值进行缩放
//            float max = Math.max(vWidth, vHeight);
//            // 计算出缩放大小,取接近的正值
//            width = (int) Math.ceil((float) width / max);
//            height = (int) Math.ceil((float) height / max);
//
//            layoutParams = new RelativeLayout.LayoutParams(width, height);
//        }

        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mVideoSurface.setLayoutParams(layoutParams);

    }


    /**
     * To change status bar to transparent.
     * @notice this method have to be used before setContentView.
     */
    private void setStatusBar() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // 4.4
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 5.0
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); // 確認取消半透明設置。(不加這句會有半透明的黑框背景)
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 全螢幕顯示，status bar 不隱藏，activity 上方 layout 會被 status bar 覆蓋。
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE); // 配合其他 flag 使用，防止 system bar 改變後 layout 的變動。
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); // 跟系統表示要渲染 system bar 背景。
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     *
     * @return height of status bar
     */
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources()
                .getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}

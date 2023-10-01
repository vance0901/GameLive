package com.vance.gamelive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.Log;

import com.vance.gamelive.codec.pool.ArrayPool;
import com.vance.gamelive.codec.AudioCodec;
import com.vance.gamelive.codec.LiveTaskManager;
import com.vance.gamelive.codec.RTMPPackage;
import com.vance.gamelive.codec.VideoCodec;
import com.vance.gamelive.codec.pool.LruArrayPool;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author vance
 * @date 2019/5/9
 */
public class ScreenLive implements Runnable {

    static {
        System.loadLibrary("native-lib");
    }

    private String url;
    private MediaProjectionManager mediaProjectionManager;
    private boolean isLiving;
    private LinkedBlockingQueue<RTMPPackage> queue = new LinkedBlockingQueue<>();
    private MediaProjection mediaProjection;


    public void startLive(Activity activity, String url) {
        this.url = url;
        // 投屏管理器
        this.mediaProjectionManager = (MediaProjectionManager) activity
                .getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        // 创建截屏请求intent
        Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
        activity.startActivityForResult(captureIntent, 100);
    }

    public void stoptLive() {
        addPackage(RTMPPackage.EMPTY_PACKAGE);
        isLiving = false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 用户授权
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            // 获得截屏器
            mediaProjection = mediaProjectionManager.getMediaProjection
                    (resultCode, data);
            LiveTaskManager.getInstance().execute(this);
        }
    }

    public void addPackage(RTMPPackage rtmpPackage) {
        if (!isLiving) {
            return;
        }
        queue.add(rtmpPackage);
    }

    private static final String TAG = "ScreenLive";

    @Override
    public void run() {
        //1、连接服务器  斗鱼rtmp服务器
        if (!connect(url)) {
            disConnect();
            return;
        }
        Log.i(TAG, "连接成功 准备推流 ==========================");
        isLiving = true;
        ArrayPool arrayPool = new LruArrayPool();
        VideoCodec videoCodec = new VideoCodec(this,arrayPool);
        videoCodec.startLive(mediaProjection);
        AudioCodec audioCodec = new AudioCodec(this,arrayPool);
        audioCodec.startLive();
        boolean isSend = true;

        while (isLiving && isSend) {
            RTMPPackage rtmpPackage = null;
            try {
                checkDrop();
                rtmpPackage = queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (null == rtmpPackage) {
                break;
            }
            byte[] buffer = rtmpPackage.getBuffer();
            if (buffer != null && buffer.length != 0) {
                isSend = sendData(buffer, rtmpPackage.getSize(),
                        rtmpPackage.getType(), rtmpPackage.getTms());
//                Log.i(TAG, "sendData: ==========================" + isSend);
            }
            arrayPool.put(buffer);
            rtmpPackage.recycle();
        }
        isLiving = false;
        videoCodec.stopLive();
        audioCodec.stopLive();
        queue.clear();
        disConnect();
    }

    private void checkDrop() throws InterruptedException {
        while (queue.size() > 200) {
            queue.take();
        }
    }

    /**
     * 与斗鱼服务器建立rtmp连接
     *
     * @param url
     * @return
     */
    private native boolean connect(String url);

    private native void disConnect();

    /**
     * 发送音视频数据到c
     *
     * @param data
     * @param len
     * @param type
     * @param tms
     * @return
     */
    private native boolean sendData(byte[] data, int len, int type, long tms);

}

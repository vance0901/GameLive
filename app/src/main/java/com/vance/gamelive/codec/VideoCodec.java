package com.vance.gamelive.codec;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Bundle;
import android.view.Surface;

import com.vance.gamelive.ScreenLive;
import com.vance.gamelive.codec.pool.ArrayPool;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author vance
 * @date 2019/5/9
 */
public class VideoCodec extends Thread {

    private ArrayPool arrayPool;
    private ScreenLive screenLive;
    private MediaCodec mediaCodec;
    private VirtualDisplay virtualDisplay;
    private boolean isLiving;
    private long timeStamp;
    private long startTime;
    private MediaProjection mediaProjection;

    public VideoCodec(ScreenLive screenLive, ArrayPool arrayPool) {
        this.screenLive = screenLive;
        this.arrayPool = arrayPool;
    }


    @Override
    public void run() {
        isLiving = true;
        mediaCodec.start();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        while (isLiving) {
            if (timeStamp != 0) {
                //2000毫秒后 mediacodec虽然设置了关键帧间隔,但是没用 需要手动强制请求
                if (System.currentTimeMillis() - timeStamp >= 2_000) {
                    Bundle params = new Bundle();
                    //立即刷新 让下一帧是关键帧
                    params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
                    mediaCodec.setParameters(params);
                    timeStamp = System.currentTimeMillis();
                }
            } else {
                timeStamp = System.currentTimeMillis();
            }
            int index = mediaCodec.dequeueOutputBuffer(bufferInfo, 10);
            if (index >= 0) {
                ByteBuffer buffer = mediaCodec.getOutputBuffer(index);
                byte[] outData = arrayPool.get(bufferInfo.size);
                buffer.get(outData, 0, bufferInfo.size);
                //sps pps
//                ByteBuffer sps = mediaCodec.getOutputFormat().getByteBuffer
//                        ("csd-0");
//                ByteBuffer pps = mediaCodec.getOutputFormat().getByteBuffer
//                        ("csd-1");
                if (startTime == 0) {
                    // 微妙转为毫秒
                    startTime = bufferInfo.presentationTimeUs / 1000;
                }

                RTMPPackage rtmpPackage = RTMPPackage.obtain();
                rtmpPackage.setBuffer(outData);
                rtmpPackage.setSize(bufferInfo.size);
                rtmpPackage.setType(RTMPPackage.RTMP_PACKET_TYPE_VIDEO);
                long tms = (bufferInfo.presentationTimeUs / 1000) - startTime;
                rtmpPackage.setTms(tms);
                screenLive.addPackage(rtmpPackage); //入队列
                mediaCodec.releaseOutputBuffer(index, false);
            }
        }
        isLiving = false;
        startTime = 0;
        mediaCodec.stop();
        mediaCodec.release();
        mediaCodec = null;
        virtualDisplay.release();
        virtualDisplay = null;
        mediaProjection.stop();
        mediaProjection = null;
    }


    public void startLive(MediaProjection mediaProjection) {
        this.mediaProjection = mediaProjection;
        // 配置编码参数
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                640,
                480);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 500_000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
        try {
            // 创建编码器
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            // 从编码器创建一个画布, 画布上的图像会被编码器自动编码
            Surface surface = mediaCodec.createInputSurface();


            virtualDisplay = mediaProjection.createVirtualDisplay(
                    "screen-codec",
                    640, 480, 1,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    surface, null, null);

        } catch (IOException e) {
            e.printStackTrace();
        }


        start();
    }

    public void stopLive() {
        isLiving = false;
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

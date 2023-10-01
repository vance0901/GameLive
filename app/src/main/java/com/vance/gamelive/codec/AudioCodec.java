package com.vance.gamelive.codec;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import com.vance.gamelive.ScreenLive;
import com.vance.gamelive.codec.pool.ArrayPool;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by vance on 2017/9/11.
 */

public class AudioCodec extends Thread {

    private final ScreenLive screenLive;
    private final ArrayPool arrayPool;


    private MediaCodec mediaCodec;
    private AudioRecord audioRecord;
    private boolean isRecoding;

    private long startTime;
    private int minBufferSize;

    public AudioCodec(ScreenLive screenLive, ArrayPool arrayPool) {
        this.screenLive = screenLive;
        this.arrayPool = arrayPool;
    }


    @Override
    public void run() {
        isRecoding = true;
        RTMPPackage rtmpPackage = new RTMPPackage();
        byte[] audioDecoderSpecificInfo = {0x12, 0x08};
        rtmpPackage.setBuffer(audioDecoderSpecificInfo);
        rtmpPackage.setType(RTMPPackage.RTMP_PACKET_TYPE_AUDIO_HEAD);
        screenLive.addPackage(rtmpPackage);
        audioRecord.startRecording();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        byte[] buffer = new byte[minBufferSize];

        while (isRecoding) {
            int len = audioRecord.read(buffer, 0, buffer.length);
            if (len <= 0) {
                continue;
            }
            //立即得到有效输入缓冲区
            int index = mediaCodec.dequeueInputBuffer(0);
            if (index >= 0) {
                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(index);
                inputBuffer.clear();
                inputBuffer.put(buffer, 0, len);
                //填充数据后再加入队列
                mediaCodec.queueInputBuffer(index, 0, len,
                        System.nanoTime() / 1000, 0);
            }
            index = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);

            while (index >= 0 && isRecoding) {
                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(index);
                byte[] outData = arrayPool.get(bufferInfo.size);
                outputBuffer.get(outData, 0, bufferInfo.size);

                if (startTime == 0) {
                    startTime = bufferInfo.presentationTimeUs / 1000;
                }
                rtmpPackage = RTMPPackage.obtain();
                rtmpPackage.setBuffer(outData);
                rtmpPackage.setSize(bufferInfo.size);
                rtmpPackage.setType(RTMPPackage.RTMP_PACKET_TYPE_AUDIO_DATA);
                long tms = (bufferInfo.presentationTimeUs / 1000) - startTime;
                rtmpPackage.setTms(tms);
                screenLive.addPackage(rtmpPackage);
                mediaCodec.releaseOutputBuffer(index, false);
                index = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }
        }
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;

        mediaCodec.stop();
        mediaCodec.release();
        mediaCodec = null;
        startTime = 0;
        isRecoding = false;
    }

    public void startLive() {

        try {

            /**
             * 获得创建AudioRecord所需的最小缓冲区
             * 采样+单声道+16位pcm
             */
            minBufferSize = AudioRecord.getMinBufferSize(44100,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            /**
             * 创建录音对象
             * 麦克风+采样+单声道+16位pcm+缓冲区大小
             */
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC, 44100,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, minBufferSize);

            MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100,
                    1);
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel
                    .AACObjectLC);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 64_000);
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, minBufferSize * 2);
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        start();
    }

    public void stopLive() {
        isRecoding = false;
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

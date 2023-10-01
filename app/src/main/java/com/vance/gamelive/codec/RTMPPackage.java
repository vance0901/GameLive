package com.vance.gamelive.codec;


/**
 * Created by vance on 2017/9/11.
 */

public class RTMPPackage {
    private static final String TAG = "RTMPPackage";

    public static final int RTMP_PACKET_TYPE_VIDEO = 0;
    public static final int RTMP_PACKET_TYPE_AUDIO_HEAD = 1;
    public static final int RTMP_PACKET_TYPE_AUDIO_DATA = 2;

    private byte[] buffer; // 图像数据
    private int type;
    private long tms;
    private int size;

    public static RTMPPackage EMPTY_PACKAGE = new RTMPPackage();


    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setTms(long tms) {
        this.tms = tms;
    }

    public long getTms() {
        return tms;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    RTMPPackage next;
    //同步对象
    public static final Object sPoolSync = new Object();
    private static RTMPPackage sPool; // 链表第一个元素（对象池）
    private static int sPoolSize = 0; //对象池中对象的个数

    private static final int MAX_POOL_SIZE = 50;


    public static RTMPPackage obtain() {
        synchronized (sPoolSync) {
            if (sPool != null) {
                RTMPPackage m = sPool; //链表第一个元素，返回出去 复用
                sPool = m.next;
                m.next = null;
                sPoolSize--;
                return m;
            }
        }
        return new RTMPPackage();
    }


    public void recycle() {
        buffer = null;
        type = -1;
        tms = -1;
        size = 0;
        synchronized (sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                next = sPool;
                sPool = this;
                sPoolSize++;
            }
        }
    }

}

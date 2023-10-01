package com.vance.gamelive.codec.pool.test;


import android.util.Log;

import com.vance.gamelive.codec.pool.ArrayPool;

import java.util.NavigableMap;
import java.util.TreeMap;

public class GlideLruArrayPool implements ArrayPool {

    private static final String TAG = "ArrayPool";

    public static final int ARRAY_POOL_SIZE_BYTES = 4 * 1024 * 1024;
    private int maxSize;

    private GlideLruMap<Integer, byte[]> glideLruMap = new GlideLruMap<>();

    // key：byte[]长度  value:个数！
    private NavigableMap<Integer, Integer> treeMap = new TreeMap<>();
    private int currentSize;

    public GlideLruArrayPool() {
        this(ARRAY_POOL_SIZE_BYTES);
    }

    public GlideLruArrayPool(int maxSize) {
        this.maxSize = maxSize;
        currentSize = 0;
    }

    /**
     * @param len
     * @return
     */
    @Override
    public synchronized byte[] get(int len) {
        //获得等于或大于比len大同时最接近len的key
        Integer key = treeMap.ceilingKey(len);
        if (key != null) {
            byte[] bytes = glideLruMap.get(key);
            if (bytes != null) {
                currentSize -= bytes.length;
                //计数器-1
                decrementArrayOfSize(key);
                return bytes;
            }
        }
        Log.i(TAG, "get: new");
        return new byte[len];
    }

    @Override
    public synchronized void put(byte[] data) {
        if (data == null || data.length == 0 || data.length > maxSize) return;
        int length = data.length;
        glideLruMap.put(length, data);
        //value ：个数
        Integer current = treeMap.get(length);
        //计数器+1
        treeMap.put(length, current == null ? 1 : current + 1);
        //保存数据的总量
        currentSize += length;
        //淘汰
        evict();
    }

    private void evict() {
        while (currentSize > maxSize) {
            byte[] evicted = glideLruMap.removeLast();
            currentSize -= evicted.length;
            //计数器-1
            decrementArrayOfSize(evicted.length);
        }
    }

    private void decrementArrayOfSize(int size) {
        Integer current = treeMap.get(size);
        if (current == 1) {
            treeMap.remove(size);
        } else {
            treeMap.put(size, current - 1);
        }
    }

    @Override
    public String toString() {
        return "ArrayPoolCustom{" +
                "lruMap=" + glideLruMap +
                '}';
    }
}

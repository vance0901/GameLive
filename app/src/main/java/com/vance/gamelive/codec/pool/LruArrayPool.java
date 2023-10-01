package com.vance.gamelive.codec.pool;


import android.util.Log;

public class LruArrayPool implements ArrayPool {

    private static final String TAG = "ArrayPool";

    public static final int ARRAY_POOL_SIZE_BYTES = 4 * 1024 * 1024;
    private int maxSize;

    private LruMap<byte[]> lruMap = new LruMap<>();

    // key：byte[]长度  value:个数！
    private TreeArray map = new TreeArray();
    private int currentSize;

    public LruArrayPool() {
        this(ARRAY_POOL_SIZE_BYTES);
    }

    public LruArrayPool(int maxSize) {
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
        int key = map.ceilingKey(len);
        if (key != 0) {
            byte[] bytes = lruMap.get(key);
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
        lruMap.put(length, data);
        //value ：个数
        int current = map.get(length);
        //计数器+1
        map.put(length, current == 0 ? 1 : current + 1);

        currentSize += length;
        evict();
    }

    private void evict() {
        while (currentSize > maxSize) {
            byte[] evicted = lruMap.removeLast();
            currentSize -= evicted.length;
            //计数器-1
            decrementArrayOfSize(evicted.length);
        }
    }

    private void decrementArrayOfSize(int key) {
        int current = map.get(key);
        if (current == 1) {
            map.delete(key);
        } else {
            map.put(key, current - 1);
        }
    }

    @Override
    public String toString() {
        return "ArrayPoolCustom{" +
                "lruMap=" + lruMap +
                '}';
    }
}

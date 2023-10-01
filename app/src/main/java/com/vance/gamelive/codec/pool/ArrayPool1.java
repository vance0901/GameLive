package com.vance.gamelive.codec.pool;


import androidx.collection.LruCache;

/**
 * @VERSION 1
 * 使用LruCache 缓存数组实现复用
 */
public class ArrayPool1 implements ArrayPool {

    private static final String TAG = "ArrayPool";

    public static final int ARRAY_POOL_SIZE_BYTES = 4 * 1024 * 1024;
    private int maxSize;

    private LruCache<Integer, byte[]> cache;

    public ArrayPool1() {
        this(ARRAY_POOL_SIZE_BYTES);
    }

    public ArrayPool1(int maxSize) {
        this.maxSize = maxSize;
        //maxSize :存放多少数据
        this.cache = new LruCache<Integer, byte[]>(maxSize) {

            //存放的每一个对象，sizeOf 确定对象的大小
            protected int sizeOf(Integer key, byte[] value) {
                return value.length;
            }

        };
    }

    /**
     * 从对象池取对象去复用
     * @param len
     * @return
     */
    @Override
    public synchronized byte[] get(int len) {
        byte[] bytes = cache.remove(len);
        if (bytes != null) {
            return bytes;
        }
        return new byte[len];
    }

    /**
     * 往对象池存对象用于复用
     * @param data
     */
    @Override
    public synchronized void put(byte[] data) {
        if (data == null || data.length == 0 || data.length > maxSize) return;
        int length = data.length;
        cache.put(length, data);
    }

}

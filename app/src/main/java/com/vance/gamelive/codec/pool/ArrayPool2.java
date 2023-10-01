package com.vance.gamelive.codec.pool;


import androidx.collection.LruCache;

import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * @VERSION 2
 * 新增TreeMap  完成缓存数组筛选，避免必须相同大小的数组才能复用
 */
public class ArrayPool2 implements ArrayPool {

    private static final String TAG = "ArrayPool";

    public static final int ARRAY_POOL_SIZE_BYTES = 4 * 1024 * 1024;
    private int maxSize;

    private LruCache<Integer, byte[]> cache;
    //key 为byte数组长度
    private final NavigableMap<Integer, Integer> sortedSizes = new TreeMap<>();

    public ArrayPool2() {
        this(ARRAY_POOL_SIZE_BYTES);
    }

    public ArrayPool2(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new LruCache<Integer, byte[]>(maxSize) {
            protected int sizeOf(Integer key, byte[] value) {
                return value.length;
            }

            protected void entryRemoved(boolean evicted, Integer key,
                                        byte[] oldValue, byte[] newValue) {
                sortedSizes.remove(oldValue.length);
            }
        };
    }

    @Override
    public synchronized byte[] get(int len) {
        //获得等于或大于len的key  10 15 5 20
        Integer key = sortedSizes.ceilingKey(len);
        if (key != null) {
            //最接近len大小的byte数组 返回复用
            byte[] bytes = cache.remove(key);
            sortedSizes.remove(key);
            return bytes;
        }
        return new byte[len];
    }

    @Override
    public synchronized void put(byte[] data) {
        if (data == null || data.length == 0 || data.length > maxSize) return;
        int length = data.length;
        sortedSizes.put(length, 1);
        cache.put(length, data);
    }

}

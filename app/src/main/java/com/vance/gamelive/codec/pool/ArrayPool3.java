package com.vance.gamelive.codec.pool;


import androidx.collection.LruCache;

import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * @VERSION 3
 * 新增计数器：以TreeMap的value作为相同大小数组的个数，避免每次只能缓存单个大小的数组
 * 问题：只是计数器在变化，实际缓存的数组无变化
 */
public class ArrayPool3 implements ArrayPool {

    private static final String TAG = "ArrayPool";

    public static final int ARRAY_POOL_SIZE_BYTES = 4 * 1024 * 1024;
    private int maxSize;

    private LruCache<Integer, byte[]> cache;
    //key 为byte数组长度，value为个数
    private final NavigableMap<Integer, Integer> sortedSizes = new TreeMap<>();

    public ArrayPool3() {
        this(ARRAY_POOL_SIZE_BYTES);
    }

    public ArrayPool3(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new LruCache<Integer, byte[]>(maxSize) {
            protected int sizeOf(Integer key, byte[] value) {
                return value.length;
            }
        };
    }

    @Override
    public synchronized byte[] get(int len) {
        //获得等于或大于比len大的key
        Integer key = sortedSizes.ceilingKey(len);
        if (key != null) {
            byte[] bytes = cache.remove(key);
            //计数器 -1
            Integer current = sortedSizes.get(key);
            if (current == 1) {
                sortedSizes.remove(key);
            } else {
                sortedSizes.put(key, current - 1);
            }
            return bytes;
        }
        return new byte[len];
    }
    @Override
    public synchronized void put(byte[] data) {
        if (data == null || data.length == 0 || data.length > maxSize) return;
        int length = data.length;
        Integer current = sortedSizes.get(length);
        //计数器+1
        sortedSizes.put(length, current == null ? 1 : current + 1);
        cache.put(length, data);
    }


}

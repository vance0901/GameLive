package com.vance.gamelive.codec.pool;


import androidx.collection.LruCache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * @VERSION 3
 * 新增LruCache value为：List
 * 问题：lrucache内部size无法更新 （put方法）
 */
public class ArrayPool4 implements ArrayPool {

    private static final String TAG = "ArrayPool";

    public static final int ARRAY_POOL_SIZE_BYTES = 4 * 1024 * 1024;
    private int maxSize;

    private LruCache<Integer, List<byte[]>> cache;
    //key 为byte数组长度，value为个数
    private final NavigableMap<Integer, Integer> sortedSizes = new TreeMap<>();

    public ArrayPool4() {
        this(ARRAY_POOL_SIZE_BYTES);
    }

    public ArrayPool4(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new LruCache<Integer, List<byte[]>>(maxSize) {
            protected int sizeOf(Integer key, List<byte[]> value) {
                return value.size() * key;
            }
        };
    }

    @Override
    public synchronized byte[] get(int len) {
        //获得等于或大于比len大的key
        Integer key = sortedSizes.ceilingKey(len);
        if (key != null) {
            List<byte[]> bytes = cache.get(key);
            byte[] data = bytes.remove(0);
            if (bytes.isEmpty()) {
                sortedSizes.remove(key);
                cache.remove(key);
            } else {
                sortedSizes.put(key, bytes.size());
            }
            return data;
        }
        return new byte[len];
    }

    @Override
    public synchronized void put(byte[] data) {
        if (data == null || data.length == 0 || data.length > maxSize) return;
        int length = data.length;
        List<byte[]> bytes = cache.get(length);
        if (bytes == null) {
            bytes = new ArrayList<>();
        }
        bytes.add(data);
        cache.put(length,bytes);
        sortedSizes.put(length, bytes.size());
    }

    @Override
    public String toString() {
        String out = "=======================================\ncache：";
        Iterator<Map.Entry<Integer, List<byte[]>>> iterator =
                cache.snapshot().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, List<byte[]>> next = iterator.next();
            out += "[" + next.getKey() + "=" + next.getValue().size() + "]";
        }
        out += "\nsizes：";
        Iterator<Map.Entry<Integer, Integer>> iterator1 = sortedSizes.entrySet().iterator();
        while (iterator1.hasNext()) {
            Map.Entry<Integer, Integer> next = iterator1.next();
            out += "[" + next.getKey() + "=" + next.getValue() + "]";
        }
        return out;
    }
}

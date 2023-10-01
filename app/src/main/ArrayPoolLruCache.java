package com.vance.gamelive.codec.pool;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;


public class ArrayPoolLruCache implements ArrayPool {

    private static final String TAG = "ArrayPool";

    public static final int ARRAY_POOL_SIZE_BYTES = 4 * 1024 * 1024;


    private LruCache<Integer, List<byte[]>> cache;
    //key 为byte数组长度，value为个数
    private final NavigableMap<Integer, Integer> sortedSizes = new TreeMap<>();
    private int currentSize;
    private int maxSize;

    public ArrayPoolLruCache() {
        this(ARRAY_POOL_SIZE_BYTES);
    }

    public ArrayPoolLruCache(int maxSize) {
        this.maxSize = maxSize;
        currentSize = 0;
        this.cache = new LruCache(maxSize) {
            @Override
            protected int sizeOf(@NonNull Object key, @NonNull Object value) {
                return 0;
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
            currentSize -= data.length;
            if (bytes.isEmpty()) {
                cache.remove(key);
                sortedSizes.remove(key);
            } else {
                sortedSizes.put(key, bytes.size());
            }
            Log.i(TAG, "get: 对象池获取bytes:" + key);
            return data;
        }
        Log.i(TAG, "get: 新建bytes:" + len);
        return new byte[len];
    }

    @Override
    public synchronized void put(byte[] data) {
        //todo glide规则：
        // 如果data数据大于maxSize的某个比例，比如data.length > maxSize*0.8 则直接抛弃
        // 避免容器被一个数组占用太多
        if (data == null || data.length == 0 || data.length > maxSize) return;
        int length = data.length;
        currentSize += length;
        List<byte[]> bytes = cache.get(length);
        if (bytes == null) {
            bytes = new ArrayList<>();
        }
        bytes.add(data);
        sortedSizes.put(length, bytes.size());
        cache.put(length, bytes);


        // 自己做淘汰
        while (currentSize > maxSize) {
            Map.Entry<Integer, List<byte[]>> next =
                    cache.snapshot().entrySet().iterator().next();
            bytes = next.getValue();
//            bytes = cache.get(next.getKey());//会导致重排序
            byte[] remove = bytes.remove(0);
            if (bytes.isEmpty()) {
                cache.remove(next.getKey());
                sortedSizes.remove(next.getKey());
            } else {
                sortedSizes.put(next.getKey(), bytes.size());
            }
            Log.i(TAG, "put: 对象池自动移除：" + next.getKey());
            currentSize -= remove.length;
        }
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

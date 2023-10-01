package com.vance.gamelive.codec.pool.test;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlideLruMap<K, V> {
    private LinkedEntry<K, V> head = new LinkedEntry<>(null);
    private final Map<K, LinkedEntry<K, V>> keyToEntry = new HashMap<>();

    public void put(K key, V value) {
        LinkedEntry<K, V> entry = keyToEntry.get(key);
        if (entry == null) {
            entry = new LinkedEntry<>(key);
            keyToEntry.put(key, entry);
        }
        makeHead(entry);
        entry.add(value);
    }

    public V get(K key) {
        LinkedEntry<K, V> entry = keyToEntry.get(key);
        if (entry != null) {
            makeHead(entry);
            return entry.removeLast();
        }
        return null;
    }


    private void makeHead(LinkedEntry<K, V> entry) {
        // 把自己前面和后面的连接到一起
        entry.prev.next = entry.next;
        entry.next.prev = entry.prev;
        //把自己放到 head 后面第一个
        entry.prev = head;
        entry.next = head.next;

        entry.next.prev = entry;
        entry.prev.next = entry;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GroupedLinkedMap( ");
        LinkedEntry<K, V> current = head.next;
        boolean hadAtLeastOneItem = false;
        while (!current.equals(head)) {
            hadAtLeastOneItem = true;
            sb.append('{').append(current.key).append(':').append(current.size()).append("}, ");
            current = current.next;
        }
        if (hadAtLeastOneItem) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.append(" )").toString();
    }

    public V removeLast() {
        LinkedEntry<K, V> last = head.prev;

        while (!last.equals(head)) {
            //移除链表最末尾的LinkedEntry中的一个byte数组
            V removed = last.removeLast();
            //如果最后一个LinkedEntry没有byte数组缓存，则将最后一个LinkedEntry移除然后循环
            if (removed != null) {
                return removed;
            } else {
                last.prev.next = last.next;
                last.next.prev = last.prev;
                keyToEntry.remove(last.key);
            }
            last = last.prev;
        }
        return null;
    }

    private static class LinkedEntry<K, V> {
        private final K key;
        private List<V> values;
        LinkedEntry<K, V> next;
        LinkedEntry<K, V> prev;

        LinkedEntry(K key) {
            this.key = key;
            prev = next = this;
        }

        @Nullable
        public V removeLast() {
            final int valueSize = size();
            return valueSize > 0 ? values.remove(valueSize - 1) : null;
        }

        public int size() {
            return values != null ? values.size() : 0;
        }

        public void add(V value) {
            if (values == null) {
                values = new ArrayList<>();
            }
            values.add(value);
        }
    }
}

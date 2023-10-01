package com.vance.gamelive.codec.pool;

import android.util.SparseArray;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LruMap<V> {
    private LinkedEntry<V> head = new LinkedEntry<>(0);
    private final SparseArray<LinkedEntry<V>> keyToEntry = new SparseArray<>();

    public void put(int key, V value) {
        LinkedEntry<V> entry = keyToEntry.get(key);
        if (entry == null) {
            entry = new LinkedEntry<>(key);
            keyToEntry.put(key, entry);
        }
        makeHead(entry);
        entry.add(value);
    }

    public V get(int key) {
        LinkedEntry<V> entry = keyToEntry.get(key);
        if (entry != null) {
            makeHead(entry);
            return entry.removeLast();
        }
        return null;
    }


    private void makeHead(LinkedEntry<V> entry) {
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
        LinkedEntry<V> current = head.next;
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
        LinkedEntry<V> last = head.prev;

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

    private static class LinkedEntry<V> {
        private final int key;
        private List<V> values;
        LinkedEntry<V> next;
        LinkedEntry<V> prev;

        LinkedEntry(int key) {
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

package com.vance.gamelive.codec.pool;

//SparseIntArray
public class TreeArray {

    public static final int[] INT = new int[0];

    private int[] mKeys;
    private int[] mValues;
    private int mSize;

    public TreeArray() {
        this(10);
    }


    public TreeArray(int initialCapacity) {
        if (initialCapacity == 0) {
            mKeys = INT;
            mValues = INT;
        } else {
            mKeys = new int[initialCapacity];
            mValues = new int[mKeys.length];
        }
        mSize = 0;
    }

    @Override
    public TreeArray clone() {
        TreeArray clone = null;
        try {
            clone = (TreeArray) super.clone();
            clone.mKeys = mKeys.clone();
            clone.mValues = mValues.clone();
        } catch (CloneNotSupportedException cnse) {
        }
        return clone;
    }


    public int get(int key) {
        return get(key, 0);
    }

    static int binarySearch(int[] array, int size, int value) {
        int lo = 0;
        int hi = size - 1;

        while (lo <= hi) {
            final int mid = (lo + hi) >>> 1;
            final int midVal = array[mid];

            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else {
                return mid;
            }
        }
        return ~lo;
    }


    public int get(int key, int valueIfKeyNotFound) {
        int i = binarySearch(mKeys, mSize, key);

        if (i < 0) {
            return valueIfKeyNotFound;
        } else {
            return mValues[i];
        }
    }


    public void delete(int key) {
        int i = binarySearch(mKeys, mSize, key);

        if (i >= 0) {
            removeAt(i);
        }
    }


    public void removeAt(int index) {
        System.arraycopy(mKeys, index + 1, mKeys, index, mSize - (index + 1));
        System.arraycopy(mValues, index + 1, mValues, index, mSize - (index + 1));
        mSize--;
    }

    public static int growSize(int currentSize) {
        return currentSize <= 4 ? 8 : currentSize * 2;
    }

    public static int[] insert(int[] array, int currentSize, int index, int element) {
        assert currentSize <= array.length;

        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        }

        int[] newArray = new int[growSize(currentSize)];
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = element;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }

    public void put(int key, int value) {
        int i = binarySearch(mKeys, mSize, key);

        if (i >= 0) {
            mValues[i] = value;
        } else {
            i = ~i;

            mKeys = insert(mKeys, mSize, i, key);
            mValues = insert(mValues, mSize, i, value);
            mSize++;
        }
    }


    public int size() {
        return mSize;
    }


    public int keyAt(int index) {
        return mKeys[index];
    }


    public int valueAt(int index) {
        return mValues[index];
    }


    @Override
    public String toString() {
        if (size() <= 0) {
            return "{}";
        }

        StringBuilder buffer = new StringBuilder(mSize * 28);
        buffer.append('{');
        for (int i = 0; i < mSize; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            int key = keyAt(i);
            buffer.append(key);
            buffer.append('=');
            int value = valueAt(i);
            buffer.append(value);
        }
        buffer.append('}');
        return buffer.toString();
    }


    public int ceilingKey(int key) {
        int r = -1;
        for (int mKey : mKeys) {
            if (mKey >= key) {
                return mKey;
            }
        }
        return r;
    }
}

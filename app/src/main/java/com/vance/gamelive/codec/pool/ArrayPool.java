package com.vance.gamelive.codec.pool;


public interface ArrayPool {

    byte[] get(int len);

    void put(byte[] data);

}

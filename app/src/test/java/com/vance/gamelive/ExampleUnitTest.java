package com.vance.gamelive;


import org.junit.Test;


import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {


    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
        NavigableMap<Integer, Integer> sortedSizes = new TreeMap<>();
        sortedSizes.put(10,1);
        sortedSizes.put(25,1);
        sortedSizes.put(15,1);
        sortedSizes.put(20,1);

        //返回最接近 14大小的存在于TreeMap中的key
        Integer integer = sortedSizes.ceilingKey(14);
    }

}
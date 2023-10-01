package com.vance.gsonexample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import jdk.internal.reflect.Reflection;

public class GsonTest {

    class Test<T> {
        int i;
        String a;
        T d;
    }

    class A {
        int j;
    }

    class B {
        String b;
    }

    public static void main(String[] args) throws Exception {


    // Test<A>
    String j1 = "{\"i\":1,\"a\":\"xxxx\",\"d\":{\"j\":1}}";
    // Test<List<A>>
    String j2 = "{\"i\":1,\"a\":\"xxxx\",\"d\":[{\"j\":1},{\"j\":2}]}";
    // Test<B>
    String j3 = "{\"i\":1,\"a\":\"xxxx\",\"d\":{\"b\":\"x1\"}}";
    // Test<List<B>>
    String j4 = "{\"i\":1,\"a\":\"xxxx\",\"d\":[{\"b\":\"x1\"},{\"b\":\"x2\"}]}";
    Gson gson = new Gson();

    Test<A> a = gson.fromJson(j1, new TypeToken<Test<A>>() {
    }.getType());
        System.out.println(a.d.j);

    Test<List<A>> b = gson.fromJson(j2, new TypeToken<Test<List<A>>>() {
    }.getType());
        System.out.println(b.d.get(1).j);

    Test<B> c = gson.fromJson(j3, new TypeToken<Test<B>>() {
    }.getType());
        System.out.println(c.d.b);

    Test<List<B>> d = gson.fromJson(j4, new TypeToken<Test<List<B>>>() {
    }.getType());
        System.out.println(d.d.get(1).b);

}


    private static void testGson1() {
        Gson gson = new Gson();
        for (int i = 0; i < 10000; i++) {
            //new TestBean() 反射  抖动
            // 频繁的new TestBean对象  --- 》 内存抖动
            TestBean testBean = gson.fromJson(
                    "{\"name\":\"vance\",\"pwd\":\"123\"}",
                    TestBean.class); // new TestBean
        }
    }

    private static void testGson2() {
        Gson gson = new GsonBuilder() // 构建者设计模式
                .registerTypeAdapter(TestBean.class, new TestTypeAdapter())
                .create();
        Type type = new TypeToken<List<TestBean>>() {
        }.getType();
        for (int i = 0; i < 1; i++) {
            TestBean testBean = gson.fromJson(
                    "{\"name\":\"vance\",\"pwd\":\"123\"}",
                    TestBean.class);
        }
    }

}

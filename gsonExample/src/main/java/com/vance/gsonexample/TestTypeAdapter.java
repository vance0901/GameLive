package com.vance.gsonexample;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class TestTypeAdapter extends TypeAdapter<TestBean> {


    @Override
    public void write(JsonWriter out, TestBean testBean) throws IOException {
        out.beginObject();
        out.name("name").value(testBean.getName())
                .name("pwd").value(testBean.getPwd());
        out.endObject();


    }



    /**
     * fromJson 反序列化时 调用
     *
     * @param in
     * @return
     * @throws IOException
     */
    @Override
    public TestBean read(JsonReader in) throws IOException {
        // 使用对象池获取  TestBean
        // 将GSON自动创建对象权限拿到了自己手上！
        TestBean bean = new TestBean(); // pool.get();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "name":
                    bean.setName(in.nextString());
                    break;
                case "pwd":
                    bean.setPwd(in.nextString());
                    break;
            }
        }
        in.endObject();
        return bean;
    }
}

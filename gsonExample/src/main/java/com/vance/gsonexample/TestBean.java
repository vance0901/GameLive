package com.vance.gsonexample;

import com.google.gson.Gson;

import java.io.Serializable;

public class TestBean implements Serializable {

    private String name;
    private String pwd;
    private transient Gson gson = new Gson();
    public TestBean(String name, String pwd) {
        this.name = name;
        this.pwd = pwd;
    }

    public TestBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    @Override
    public String toString() {
        return "TestBean{" +
                "name='" + name + '\'' +
                ", pwd='" + pwd + '\'' +
                '}';
    }
}

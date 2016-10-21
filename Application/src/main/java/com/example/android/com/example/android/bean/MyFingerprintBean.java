package com.example.android.com.example.android.bean;

/**
 * Created by howell on 2016/10/21.
 */

public class MyFingerprintBean {
    int id;
    String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MyFingerprintBean(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public MyFingerprintBean(int id) {
        this.id = id;
    }

    public MyFingerprintBean(String name) {
        this.name = name;
    }

    public MyFingerprintBean() {
    }

    @Override
    public String toString() {
        return "MyFingerprintBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

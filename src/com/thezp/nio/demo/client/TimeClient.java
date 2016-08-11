package com.thezp.nio.demo.client;

/**
 * Created by paopao on 2016/8/11.
 */
public class TimeClient {
    public static void main(String[] args) {
        int port = 8080;
        new Thread(new TimeClientHandle(null, port),"client").start();
    }
}

package com.thezp.nio.demo.server;

/**
 * Created by paopao on 2016/8/11.
 */
public class TimeServer {
    public static void main(String[] args) {
        int port = 8080;
        MultiplexerTimeServer mts = new MultiplexerTimeServer(port);
        new Thread(mts, "mts").start();
    }
}

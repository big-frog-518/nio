package com.thezp.nio.demo.server;

import com.sun.xml.internal.ws.api.server.LazyMOMProvider;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by paopao on 2016/8/11.
 */
public class MultiplexerTimeServer implements Runnable {
    private Selector selector;

    private ServerSocketChannel ssc;

    private volatile boolean stop;

    public MultiplexerTimeServer(int port) {
        try {
            selector = Selector.open();
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.socket().bind(new InetSocketAddress(port), 1024);
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("time server ready");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                selector.select(1000);
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                SelectionKey key = null;
                while(it.hasNext()) {
                    key = it.next();
                    it.remove();
                    try {
                        handleInput(key);
                    } catch (Exception e1) {
                        if(key != null) {
                            key.cancel();
                            if(key.channel() != null) {
                                key.channel().close();
                            }
                        }
                        e1.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(selector != null) {
            try{
                selector.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    private void handleInput(SelectionKey key) throws Exception{
        if(key.isValid()) {
            if(key.isAcceptable()) {
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept();
                sc.configureBlocking(false);
                sc.register(selector, SelectionKey.OP_READ);
            }

            if(key.isReadable()) {
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                if(readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("receive : " + body);
                    String time = "QTO".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD Order";
                    doWrite(sc, time);
                } else if(readBytes < 0) {
                    key.cancel();
                    sc.close();
                } else {
                }
            }
        }
    }

    private void doWrite(SocketChannel sc, String time) throws Exception{
        if(time != null && time.trim().length() > 0) {
            byte[] bytes = time.getBytes();
            ByteBuffer bb = ByteBuffer.allocate(bytes.length);
            bb.put(bytes);
            bb.flip();
            sc.write(bb);
        }
    }
}

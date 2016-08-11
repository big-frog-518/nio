package com.thezp.nio.demo.client;

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
public class TimeClientHandle implements Runnable {
    private String host;
    private int port;
    private Selector selector;
    private SocketChannel sc;
    private volatile boolean stop;

    public TimeClientHandle(String host, int port) {
        this.host = host == null ? "127.0.0.1" : host;
        this.port = port;
        try{
            selector = Selector.open();
            sc = SocketChannel.open();
            sc.configureBlocking(false);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        try{
            doConnect();
        } catch (Exception e1) {
            e1.printStackTrace();
            System.exit(1);
        }

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
            SocketChannel sc = (SocketChannel)key.channel();

            if(key.isConnectable()) {
                if(sc.finishConnect()) {
                    sc.register(selector, SelectionKey.OP_READ);
                    doWrite(sc);
                } else {
                    System.exit(1);
                }
            }

            if(key.isReadable()) {
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                if(readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("now is : " + body);
                    this.stop = true;
                } else if(readBytes < 0) {
                    key.cancel();
                    sc.close();
                } else {
                }
            }
        }
    }

    private void doWrite(SocketChannel sc) throws Exception {
        byte[] req = "QTO".getBytes();
        ByteBuffer bb = ByteBuffer.allocate(req.length);
        bb.put(req);
        bb.flip();
        sc.write(bb);
        if(!bb.hasRemaining()) {
            System.out.println("send order 2 server success");
        }
    }

    private void doConnect() throws Exception{
        if(sc.connect(new InetSocketAddress(host,port))) {
            System.out.println("in connect");
            sc.register(selector, SelectionKey.OP_READ);
        } else {
            System.out.println("in not connect");
            sc.register(selector, SelectionKey.OP_CONNECT);
        }
    }
}

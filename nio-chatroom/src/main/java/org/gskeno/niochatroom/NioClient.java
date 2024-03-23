package org.gskeno.niochatroom;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.Set;

/**
 * NIO客户端
 */
public class NioClient {

    /**
     * 启动
     */
    public void start(String nickname) throws IOException {
        /**
         * 连接服务器端
         */
        SocketChannel socketChannel = SocketChannel.open();

        /**
         * 接收服务器端响应
         */
        // 新开线程，专门负责来接收服务器端的响应数据
        // selector ， socketChannel ， 注册
        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        boolean connect = socketChannel.connect(new InetSocketAddress("127.0.0.1", 8000));
        System.out.println("connect:" + connect);
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        int numReadyKeys = selector.select(5000);
        System.out.println("numReadyKeys:" + numReadyKeys);
        if (numReadyKeys > 0){
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            for(SelectionKey k : readyKeys){
                System.out.println(k);
                // selectionKey的可连接，并不代表tcp的握手连接成功
                if (k.isConnectable()){
                    SocketChannel channelA = (SocketChannel)k.channel();
                    // 这里连接成功，才算成功
                    boolean b = channelA.finishConnect();
                    System.out.println("finishConnect:" + b);
                }
            }
        }
        // 连接已经成功
        socketChannel.register(selector, SelectionKey.OP_READ);
        new Thread(new NioClientHandler(selector)).start();

        /**
         * 向服务器端发送数据
         */
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String request = scanner.nextLine();
            if (request != null && request.length() > 0) {
                socketChannel.write(
                        Charset.forName("UTF-8")
                                .encode(nickname + " : " + request));
            }
        }

    }

    public static void main(String[] args) throws IOException {
        new NioClient().start("clientX");
    }

}

package org.gskeno.kafka.example.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * {@link  org.apache.kafka.common.network.Selector}
 */
public class SelectorTest {
    public static void main(String[] args) throws IOException {
        Selector nioSelector = Selector.open();
        // 创建网络通道
        SocketChannel socketChannel = SocketChannel.open();
        // 设置为非阻塞
        socketChannel.configureBlocking(false);
        // 获取通道上的socket
        Socket socket = socketChannel.socket();
        // 如果这个连接上双方任意方向在2小时之内没有发送过数据，那么tcp会自动发送一个探测探测包给对方，这种探测包对方是必须回应的，回应结果有三种
        socket.setKeepAlive(true);
        // TCP_NODELAY是一种套接字选项，用于控制Nagle算法的启用和禁用。Nagle算法通过在数据包小于特定大小时暂存小数据包，然后在可能的情况下一次发送多个数据包，
        // 来减少网络中的小数据包数量，从而减少网络拥塞。
        // 但是，有些情况下，例如实时通信应用，我们可能希望数据尽快发送，而不是等到数据包变得足够大。在这种情况下，我们可以禁用Nagle算法，即设置TCP_NODELAY为true。
        socket.setTcpNoDelay(true);

        InetSocketAddress address =
                new InetSocketAddress("127.0.0.1", 8082);
        // new InetSocketAddress("baidu.com", 80);
        // 如果连接建立很快，返回true；否则返回false，后续通过finishConnect查看连接是否已经完成建立
        boolean connect = socketChannel.connect(address);
        System.out.println("connect:" + connect);

        // 将channel注册到selector上，返回一个key
        SelectionKey key = socketChannel.register(nioSelector, SelectionKey.OP_CONNECT);
        // 可将一个对象附到SelectionKey，方便识别channel
        key.attach(socketChannel);

        // 查询已经就绪的key数量
        System.out.println(System.currentTimeMillis());
        int numReadyKeys = nioSelector.select(5000);
        System.out.println(System.currentTimeMillis());
        System.out.println("numReadyKeys:" + numReadyKeys);

        if (numReadyKeys > 0){
            Set<SelectionKey> readyKeys = nioSelector.selectedKeys();
            for(SelectionKey k : readyKeys){
                System.out.println(k);
                // selectionKey的可连接，并不代表tcp的握手连接成功
                if (k.isConnectable()){
                    SocketChannel channelA = (SocketChannel)k.channel();
                    // 这里连接成功，才算成功;如果无法连接这里会发生异常
                    boolean b = channelA.finishConnect();
                    System.out.println("finishConnect:" + b);
                }
            }
        }
    }
}

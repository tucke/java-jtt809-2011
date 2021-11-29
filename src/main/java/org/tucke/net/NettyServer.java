package org.tucke.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author tucke
 */
@Slf4j
public class NettyServer {

    private final String name;
    private final ChannelHandler handler;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyServer(String name, ChannelHandler handler) {
        this.name = name;
        this.handler = handler;
    }

    public void bind(int port) throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        // 配置NIO线程组
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                // BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。
                .option(ChannelOption.SO_BACKLOG, 128)
                // Socket参数，连接保活，默认值为False。启用该功能时，TCP会主动探测空闲连接的有效性。
                // 可以将此功能视为TCP的心跳机制，需要注意的是：默认的心跳间隔是7200s即2小时。Netty默认关闭该功能。
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                // 在TCP/IP协议中，无论发送多少数据，总是要在数据前面加上协议头，同时，对方接收到数据，也需要发送ACK表示确认。
                // 为了尽可能的利用网络带宽，TCP总是希望尽可能的发送足够大的数据。这里就涉及到一个名为Nagle的算法，该算法的目的就是为了尽可能发送大块数据，避免网络中充斥着许多小数据块。
                // TCP_NODELAY就是用于启用或关于Nagle算法。如果要求高实时性，有数据发送时就马上发送，就将该选项设置为true关闭Nagle算法；如果要减少发送次数减少网络交互，就设置为false等累积一定大小后再发送。默认为false。
                .childOption(ChannelOption.TCP_NODELAY, true)
                // Netty参数，ByteBuf的分配器(重用缓冲区)
                .childOption(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .childHandler(handler);
        // 绑定端口同步等待
        ChannelFuture future = bootstrap.bind(port).sync();
        log.debug("{} server start listen at {}", name, port);
        // 等待监听端口关闭
        future.channel().closeFuture().addListener(f -> shutdown());
    }

    public void shutdown() {
        // 优雅退出，释放线程池资源
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}

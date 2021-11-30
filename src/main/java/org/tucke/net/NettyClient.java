package org.tucke.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author tucke
 */
@Slf4j
@Data
public class NettyClient {

    private final String name;
    private final SocketAddress address;
    private final ChannelHandler handler;

    private EventLoopGroup group;

    public NettyClient(String name, SocketAddress address, ChannelHandler handler) {
        this.name = name;
        this.address = address;
        this.handler = handler;
    }

    public NettyClient(String name, String host, int port, ChannelHandler handler) {
        this.name = name;
        this.address = new InetSocketAddress(host, port);
        this.handler = handler;
    }

    public void connect() throws Exception {
        // 配置客户端NIO线程组
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(address)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(handler);
        ChannelFuture channelFuture = bootstrap.connect();
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                log.info("{}连接成功，远端地址为：{}", name, address.toString());
            }
        });
    }

    public void shutdown() {
        // 优雅退出，释放线程池资源
        if (group != null) {
            group.shutdownGracefully();
        }
    }

}

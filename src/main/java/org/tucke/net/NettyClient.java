package org.tucke.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author tucke
 */
@Slf4j
@Data
public class NettyClient {

    private final String host;
    private final int port;
    private final ChannelHandler handler;

    private EventLoopGroup group;
    private Channel channel;

    public NettyClient(String host, int port, ChannelHandler handler) {
        this.host = host;
        this.port = port;
        this.handler = handler;
    }

    public void connect() throws Exception {
        // 配置客户端NIO线程组
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(host, port)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(handler);
        ChannelFuture channelFuture = bootstrap.connect();
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                channel = channelFuture.channel();
            }
        });
    }

    public void write(Object object) {
        if (channel != null && channel.isActive()) {
            this.channel.write(object);
        }
    }

    public void writeAndFlush(Object object) {
        if (channel != null && channel.isActive()) {
            this.channel.writeAndFlush(object);
        }
    }

    public void shutdown() {
        // 优雅退出，释放线程池资源
        if (group != null) {
            group.shutdownGracefully();
        }
    }

}

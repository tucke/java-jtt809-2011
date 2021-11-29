package org.tucke.jtt809;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.tucke.config.AppConfig;
import org.tucke.jtt809.common.Jtt809Constant;
import org.tucke.jtt809.decoder.Jtt809Decoder;
import org.tucke.jtt809.encoder.Jtt809Encoder;
import org.tucke.jtt809.handler.master.Jtt809MasterInboundHandler;
import org.tucke.jtt809.handler.master.Jtt809MasterOutboundHandler;
import org.tucke.net.NettyServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主链接
 *
 * @author tucke
 */
@SuppressWarnings({"SpellCheckingInspection", "DuplicatedCode"})
public class Jtt809Server {

    private static final ChannelGroup GROUP = new DefaultChannelGroup("Jtt809Server", GlobalEventExecutor.INSTANCE);
    private static final Map<Integer, ChannelId> ID_MAP = new ConcurrentHashMap<>();

    private volatile static Jtt809Server instance;
    private NettyServer nettyServer;

    private Jtt809Server() {
    }

    public static Jtt809Server getInstance() {
        if (instance == null) {
            synchronized (Jtt809Server.class) {
                if (instance == null) {
                    instance = new Jtt809Server();
                }
            }
        }
        return instance;
    }

    public void start() throws Exception {
        // 数据包结束标识
        ByteBuf packetEndFlag = Unpooled.wrappedBuffer(new byte[]{Jtt809Constant.PACKET_END_FLAG});
        nettyServer = new NettyServer(Jtt809Constant.SERVER_NAME, new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                // 连续 3min 未收到下级平台发送的从链路保持应答数据包，则认为下级平台已经失去连接，将主动断开数据传输从链路。
                // 考虑网络问题，这里设置为 5 分钟
                ch.pipeline().addLast(new ReadTimeoutHandler(300));
                ch.pipeline().addLast(new Jtt809MasterOutboundHandler());
                ch.pipeline().addLast(new Jtt809Encoder());
                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(2048, packetEndFlag));
                ch.pipeline().addLast(new Jtt809Decoder());
                ch.pipeline().addLast(new Jtt809MasterInboundHandler());
            }
        });
        nettyServer.bind(AppConfig.getIntValue("jtt809.port"));
    }

    public void stop() {
        nettyServer.shutdown();
    }

    public static void add(Integer gnsscenterId, Channel channel) {
        ChannelId channelId = channel.id();
        ID_MAP.put(gnsscenterId, channelId);
        GROUP.add(channel);
    }

    public static Channel find(Integer gnsscenterId) {
        ChannelId channelId = ID_MAP.get(gnsscenterId);
        if (channelId == null) {
            return null;
        }
        Channel channel = GROUP.find(channelId);
        if (channel == null) {
            ID_MAP.remove(gnsscenterId);
        }
        return channel;
    }

    public static void write(Integer gnsscenterId, Object message) {
        Channel channel = find(gnsscenterId);
        if (channel != null) {
            channel.write(message);
        }
    }

    public static void writeAndFlush(Integer gnsscenterId, Object message) {
        Channel channel = find(gnsscenterId);
        if (channel != null) {
            channel.writeAndFlush(message);
        }
    }

}

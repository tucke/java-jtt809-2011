package org.tucke.jtt809;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.tucke.gnsscenter.GnssCenterService;
import org.tucke.jtt809.common.Jtt809Constant;
import org.tucke.jtt809.decoder.Jtt809Decoder;
import org.tucke.jtt809.encoder.Jtt809Encoder;
import org.tucke.jtt809.handler.slave.Jtt809SlaveInboundHandler;
import org.tucke.jtt809.packet.connect.UpConnectPacket;
import org.tucke.jtt809.packet.common.OuterPacket;
import org.tucke.net.NettyClient;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 从链接
 *
 * @author tucke
 */
@SuppressWarnings({"RedundantThrows", "SpellCheckingInspection", "DuplicatedCode"})
@Slf4j
public class Jtt809Client {

    private static final ChannelGroup GROUP = new DefaultChannelGroup("Jtt809Client", GlobalEventExecutor.INSTANCE);
    private static final Map<Integer, ChannelId> ID_MAP = new ConcurrentHashMap<>();
    private static final Map<Integer, AtomicInteger> RETRY_COUNT = new ConcurrentHashMap<>();

    public static void newClient(Integer gnsscenterId, SocketAddress address, OuterPacket activePacket) {
        ByteBuf packetEndFlag = Unpooled.wrappedBuffer(new byte[]{Jtt809Constant.PACKET_END_FLAG});
        String clientName = "下级平台 " + gnsscenterId + " 服务器";
        NettyClient client = new NettyClient(clientName, address, new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                // 每分钟（这里设置为 55 秒）进行心跳检查
                ch.pipeline().addLast(new IdleStateHandler(0, 55, 0, TimeUnit.SECONDS));
                ch.pipeline().addLast(new Jtt809Encoder());
                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(2048, packetEndFlag));
                ch.pipeline().addLast(new Jtt809Decoder());
                ch.pipeline().addLast(new Jtt809SlaveInboundHandler(gnsscenterId, activePacket));
            }
        });
        try {
            client.connect();
        } catch (Exception e) {
            downDisconnectInform(gnsscenterId, (byte) 0x00);
            log.error("连接下级平台 {} 失败：{}", gnsscenterId, e);
        }
    }

    public static void newClient(Integer gnsscenterId, String host, int port, OuterPacket activePacket) {
        newClient(gnsscenterId, new InetSocketAddress(host, port), activePacket);
    }

    public static void createClient(Integer gnsscenterId, SocketAddress address, OuterPacket activePacket) {
        // 如果存在该从链接，则不用创建
        Channel channel = find(gnsscenterId);
        if (channel != null && channel.isActive()) {
            return;
        }
        newClient(gnsscenterId, address, activePacket);
    }

    @SuppressWarnings("AlibabaUndefineMagicConstant")
    public static void reconnect(Integer gnsscenterId, OuterPacket activePacket) {
        int retry = 0;
        if (RETRY_COUNT.containsKey(gnsscenterId)) {
            retry = RETRY_COUNT.get(gnsscenterId).getAndIncrement();
        } else {
            RETRY_COUNT.put(gnsscenterId, new AtomicInteger());
        }
        if (retry < 3) {
            log.debug("第 {} 次尝试重连下级平台 {} 服务器。。。", retry + 1, gnsscenterId);
            UpConnectPacket.Request request = GnssCenterService.getInstance().getDownRequest(gnsscenterId);
            if (request == null) {
                log.warn("无法重连");
                return;
            }
            newClient(gnsscenterId, request.getDownLinkIp(), request.getDownLinkPort(), activePacket);
        } else {
            log.debug("从链路下级平台 {} 服务器超过重连次数，不再进行重连，并且通过主链路通知下级平台", gnsscenterId);
            downDisconnectInform(gnsscenterId, (byte) 0x01);
            RETRY_COUNT.remove(gnsscenterId);
            close(gnsscenterId);
            // 通过抛出异常的方式关闭重试任务
            throw new RuntimeException("超过重试次数");
        }
    }

    /**
     * 主链路通知下级平台从链路断开
     *
     * @param reason 0x00：无法连接下级平台指定的服务IP与端口
     *               0x01：上级平台客户端与下级平台服务端断开
     *               0x02：其他原因
     */
    private static void downDisconnectInform(Integer gnsscenterId, byte reason) {
        Channel masterChannel = Jtt809Server.find(gnsscenterId);
        if (masterChannel != null) {
            OuterPacket out = new OuterPacket(Jtt809Constant.DataType.DOWN_DISCONNECT_INFORM, Unpooled.wrappedBuffer(new byte[]{reason}));
            masterChannel.writeAndFlush(out);
        }
    }

    public static void resetRetryCount(Integer gnsscenterId) {
        if (RETRY_COUNT.containsKey(gnsscenterId)) {
            RETRY_COUNT.get(gnsscenterId).set(0);
        } else {
            RETRY_COUNT.put(gnsscenterId, new AtomicInteger());
        }
    }

    @SuppressWarnings("DuplicatedCode")
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

    public static void add(Integer gnsscenterId, Channel channel) {
        resetRetryCount(gnsscenterId);
        ChannelId channelId = channel.id();
        ID_MAP.put(gnsscenterId, channelId);
        GROUP.add(channel);
    }

    public static void close(Integer gnsscenterId) {
        Channel channel = find(gnsscenterId);
        if (channel != null) {
            channel.close();
        }
        ID_MAP.remove(gnsscenterId);
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

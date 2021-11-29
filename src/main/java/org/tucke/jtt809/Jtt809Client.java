package org.tucke.jtt809;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.tucke.jtt809.common.Jtt809Constant;
import org.tucke.jtt809.decoder.Jtt809Decoder;
import org.tucke.jtt809.encoder.Jtt809Encoder;
import org.tucke.jtt809.handler.slave.Jtt809SlaveInboundHandler;
import org.tucke.jtt809.packet.UpConnectPacket;
import org.tucke.jtt809.packet.common.OuterPacket;
import org.tucke.net.NettyClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 从链接
 *
 * @author tucke
 */
@SuppressWarnings({"RedundantThrows", "SpellCheckingInspection"})
@Slf4j
public class Jtt809Client {

    private static final Map<Integer, NettyClient> CLIENT_MAP = new ConcurrentHashMap<>();

    public static void createClient(Integer gnsscenterId, UpConnectPacket.Request request, OuterPacket activePacket) {
        // 如果存在该从链接，则不用创建
        if (CLIENT_MAP.containsKey(gnsscenterId)) {
            NettyClient client = CLIENT_MAP.get(gnsscenterId);
            if (client != null && client.getChannel().isActive()) {
                return;
            }
        }
        newClient(gnsscenterId, request, activePacket);
    }

    public static void newClient(Integer gnsscenterId, UpConnectPacket.Request request, OuterPacket activePacket) {
        ByteBuf packetEndFlag = Unpooled.wrappedBuffer(new byte[]{Jtt809Constant.PACKET_END_FLAG});
        NettyClient client = new NettyClient(request.getDownLinkIp(), request.getDownLinkPort(), new ChannelInitializer<>() {
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
            CLIENT_MAP.put(gnsscenterId, client);
        } catch (Exception e) {
            log.error("连接下级平台 {} 失败：{}", gnsscenterId, e.getLocalizedMessage());
        }
    }

    public static void removeClient(ChannelHandlerContext ctx, Integer gnsscenterId) {
        if (CLIENT_MAP.containsKey(gnsscenterId)) {
            ctx.close();
            CLIENT_MAP.remove(gnsscenterId);
        }
    }

    public static void writeAndFlush(Integer gnsscenterId, byte[] bytes) {
        CLIENT_MAP.get(gnsscenterId).writeAndFlush(bytes);
    }

}

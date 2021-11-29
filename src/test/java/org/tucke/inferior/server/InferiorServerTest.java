package org.tucke.inferior.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.tucke.jtt809.common.Jtt809Constant;
import org.tucke.jtt809.decoder.Jtt809Decoder;
import org.tucke.jtt809.encoder.Jtt809Encoder;
import org.tucke.jtt809.handler.master.Jtt809MasterOutboundHandler;
import org.tucke.net.NettyServer;

public class InferiorServerTest {

    public static void main(String[] args) throws InterruptedException {
        ByteBuf packetEndFlag = Unpooled.wrappedBuffer(new byte[]{Jtt809Constant.PACKET_END_FLAG});
        NettyServer nettyServer = new NettyServer(Jtt809Constant.SERVER_NAME, new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                // 连续 3min 未收到下级平台发送的从链路保持应答数据包，则认为下级平台已经失去连接，将主动断开数据传输从链路。
                // 考虑网络问题，这里设置为 5 分钟
                ch.pipeline().addLast(new ReadTimeoutHandler(300));
                ch.pipeline().addLast(new Jtt809MasterOutboundHandler());
                ch.pipeline().addLast(new Jtt809Encoder());
                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(2048, packetEndFlag));
                ch.pipeline().addLast(new Jtt809Decoder());
                ch.pipeline().addLast(new InferiorServerInboundHandler());
            }
        });
        nettyServer.bind(5300);
    }

}

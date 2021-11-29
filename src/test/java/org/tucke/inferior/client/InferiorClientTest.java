package org.tucke.inferior.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.tucke.jtt809.common.Jtt809Constant;
import org.tucke.jtt809.decoder.Jtt809Decoder;
import org.tucke.jtt809.encoder.Jtt809Encoder;
import org.tucke.net.NettyClient;

import java.util.concurrent.TimeUnit;

public class InferiorClientTest {

    public static void main(String[] args) throws Exception {
        ByteBuf packetEndFlag = Unpooled.wrappedBuffer(new byte[]{Jtt809Constant.PACKET_END_FLAG});

        NettyClient client = new NettyClient("localhost", 5200, new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new IdleStateHandler(0, 55, 0, TimeUnit.SECONDS));
                ch.pipeline().addLast(new Jtt809Encoder());
                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(2048, packetEndFlag));
                ch.pipeline().addLast(new Jtt809Decoder());
                ch.pipeline().addLast(new InferiorClientInboundHandler());
            }
        });
        client.connect();
    }

}

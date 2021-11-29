package org.tucke.inferior.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.tucke.jtt809.packet.common.OuterPacket;

public class InferiorServerInboundHandler extends SimpleChannelInboundHandler<OuterPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, OuterPacket msg) throws Exception {
        System.out.println(msg);
    }

}

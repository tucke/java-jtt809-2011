package org.tucke.inferior.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.tucke.jtt809.common.Jtt809Constant;
import org.tucke.jtt809.packet.common.OuterPacket;

import java.nio.charset.Charset;

public class InferiorClientInboundHandler extends SimpleChannelInboundHandler<OuterPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, OuterPacket msg) throws Exception {
        System.out.println(msg.toString());
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelRegistered");
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelUnregistered");
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive");
        ctx.channel().attr(Jtt809Constant.NettyAttribute.GNSS_CENTER_ID).setIfAbsent("95277");

        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeInt(2021);
        byte[] pwd = new byte[8];
        byte[] pwdBytes = "1234560".getBytes(Charset.forName("GBK"));
        System.arraycopy(pwdBytes, 0, pwd, 0, pwdBytes.length);
        byteBuf.writeBytes(pwd);
        byte[] ip = new byte[32];
        byte[] ipBytes = "192.168.12.6".getBytes(Charset.forName("GBK"));
        System.arraycopy(ipBytes, 0, ip, 0, ipBytes.length);
        byteBuf.writeBytes(ip);
        byteBuf.writeShort(5300);

        OuterPacket packet = new OuterPacket();
        packet.setId(Jtt809Constant.DataType.UP_CONNECT_REQ);
        packet.setBody(ByteBufUtil.getBytes(byteBuf));
        byteBuf.release();

        ctx.writeAndFlush(packet);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelInactive");
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                OuterPacket packet = new OuterPacket();
                packet.setId(Jtt809Constant.DataType.UP_LINKTEST_REQ);
                ctx.writeAndFlush(packet);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("exceptionCaught");
        super.exceptionCaught(ctx, cause);
    }

}

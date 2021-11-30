package org.tucke.jtt809.handler.slave;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tucke.jtt809.Jtt809Client;
import org.tucke.jtt809.common.Jtt809Constant;
import org.tucke.jtt809.packet.common.OuterPacket;

import java.util.concurrent.TimeUnit;

/**
 * @author tucke
 */
@SuppressWarnings("SpellCheckingInspection")
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Jtt809SlaveInboundHandler extends SimpleChannelInboundHandler<OuterPacket> {

    private Integer gnsscenterId;
    private OuterPacket activePacket;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, OuterPacket msg) throws Exception {

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("从链路下级平台 {} 通道激活", gnsscenterId);
        Jtt809Client.add(gnsscenterId, ctx.channel());
        if (activePacket != null) {
            ctx.writeAndFlush(activePacket);
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("从链路下级平台 {} 通道断开, 准备重连。。。", gnsscenterId);
        EventLoop eventLoop = ctx.channel().eventLoop();
        // 每 5 秒进行一次重连
        eventLoop.scheduleWithFixedDelay(() -> Jtt809Client.reconnect(gnsscenterId, activePacket), 5, 5, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                // 从链路连接保持请求
                OuterPacket packet = new OuterPacket();
                packet.setId(Jtt809Constant.DataType.DOWN_LINKTEST_REQ);
                ctx.writeAndFlush(packet);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("从链路下级平台 {} 异常：{}", gnsscenterId, cause);
        super.exceptionCaught(ctx, cause);
    }
}

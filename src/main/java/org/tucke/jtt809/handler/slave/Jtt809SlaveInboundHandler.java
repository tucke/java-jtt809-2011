package org.tucke.jtt809.handler.slave;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tucke.jtt809.Jtt809Client;
import org.tucke.jtt809.common.Jtt809Constant;
import org.tucke.jtt809.packet.common.OuterPacket;

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
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (activePacket != null) {
            ctx.writeAndFlush(activePacket);
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        Jtt809Client.removeClient(ctx, gnsscenterId);
        super.channelUnregistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Jtt809Client.removeClient(ctx, gnsscenterId);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                OuterPacket packet = new OuterPacket();
                packet.setId(Jtt809Constant.DataType.DOWN_LINKTEST_REQ);
                ctx.writeAndFlush(packet);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

}

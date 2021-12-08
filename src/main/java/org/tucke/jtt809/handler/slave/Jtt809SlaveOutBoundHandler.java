package org.tucke.jtt809.handler.slave;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

/**
 * @author tucke
 */
@Slf4j
public class Jtt809SlaveOutBoundHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("从链路发送的消息：{}", ByteBufUtil.hexDump((ByteBuf) msg));
        }
        super.write(ctx, msg, promise);
    }

}

package org.tucke.jtt809.handler.master;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.tucke.jtt809.handler.ProtocolHandler;
import org.tucke.jtt809.handler.protocol.connect.ConnectProtocol;
import org.tucke.jtt809.handler.protocol.exg.VehicleExgProtocol;
import org.tucke.jtt809.packet.common.OuterPacket;

/**
 * @author tucke
 */
@Slf4j
public class Jtt809MasterInboundHandler extends SimpleChannelInboundHandler<OuterPacket> {

    private final ProtocolHandler protocolHandler;

    public Jtt809MasterInboundHandler() {
        super(true);
        protocolHandler = new ProtocolHandler();
        protocolHandler.addProtocol(new ConnectProtocol());
        protocolHandler.addProtocol(new VehicleExgProtocol());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, OuterPacket msg) throws Exception {
        protocolHandler.handle(ctx, msg);
    }

}

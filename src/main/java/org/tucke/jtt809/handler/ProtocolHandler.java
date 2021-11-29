package org.tucke.jtt809.handler;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.tucke.jtt809.handler.protocol.Protocol;
import org.tucke.jtt809.packet.common.OuterPacket;

import java.util.HashSet;
import java.util.Set;

/**
 * @author tucke
 */
@Slf4j
public class ProtocolHandler {

    private final Set<Protocol> protocols = new HashSet<>();

    public void addProtocol(Protocol protocol) {
        protocols.add(protocol);
    }

    public void handle(ChannelHandlerContext ctx, OuterPacket packet) throws Exception {
        for (Protocol protocol : protocols) {
            if (protocol.support(packet.getId())) {
                protocol.handle(ctx, packet);
                return;
            }
        }
    }

}

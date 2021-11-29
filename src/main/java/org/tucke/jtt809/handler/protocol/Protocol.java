package org.tucke.jtt809.handler.protocol;

import io.netty.channel.ChannelHandlerContext;
import org.tucke.jtt809.packet.common.OuterPacket;

/**
 * 协议处理接口类
 *
 * @author tucke
 */
public interface Protocol {

    /**
     * 判断当前处理器是否能处理该数据类型
     *
     * @param id 数据类型
     * @return ture or false
     */
    boolean support(int id);

    /**
     * 协议处理逻辑
     *
     * @param ctx    netty 上下文
     * @param packet 外层包
     */
    void handle(ChannelHandlerContext ctx, OuterPacket packet);

}

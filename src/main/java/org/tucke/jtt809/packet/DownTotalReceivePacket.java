package org.tucke.jtt809.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;

/**
 * @author tucke
 */
@Data
public class DownTotalReceivePacket {

    private Integer dynamicInfoTotal;
    private Long startTime;
    private Long endTime;

    /**
     * 编码登录回复报文
     */
    public static ByteBuf encode(DownTotalReceivePacket packet) {
        ByteBuf byteBuf = Unpooled.buffer(5);
        byteBuf.writeInt(packet.getDynamicInfoTotal());
        byteBuf.writeLong(packet.getStartTime());
        byteBuf.writeLong(packet.getEndTime());
        return byteBuf;
    }

}

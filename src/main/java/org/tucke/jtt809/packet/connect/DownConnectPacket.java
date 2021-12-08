package org.tucke.jtt809.packet.connect;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

/**
 * @author tucke
 */
public class DownConnectPacket {

    public static byte[] encode(int verifyCode) {
        ByteBuf byteBuf = Unpooled.buffer(4);
        byteBuf.writeInt(verifyCode);
        byte[] bytes = ByteBufUtil.getBytes(byteBuf);
        byteBuf.release();
        return bytes;
    }

}

package org.tucke.jtt809.packet.connect;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tucke.jtt809.common.Jtt809Util;

/**
 * @author tucke
 */
public class UpDisConnectPacket {

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Request {
        /**
         * 用户名
         */
        private int userId;
        /**
         * 密码
         */
        private String password;
    }

    /**
     * 解析注销报文
     */
    public static Request decode(ByteBuf byteBuf) {
        int userId = byteBuf.readInt();
        String password = Jtt809Util.readGBKString(byteBuf, 8);
        return new Request(userId, password);
    }

}

package org.tucke.jtt809.packet.connect;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tucke.jtt809.common.Jtt809Util;

/**
 * @author tucke
 */
public class UpConnectPacket {

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
        /**
         * 下级平台提供对应的从链路服务端 IP 地址
         */
        private String downLinkIp;
        /**
         * 下级平台提供对应的从链路服务器端口号
         */
        private short downLinkPort;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Response {
        /**
         * 验证结果
         * <p>
         * 0x00 - 成功
         * 0x01 - IP 地址不正确
         * 0x02 - 接入码不正确
         * 0x03 - 用户没用注册
         * 0x04 - 密码错误
         * 0x05 - 资源紧张，稍后再连接(已经占用)
         * 0x06 - 其他
         */
        private byte result;
        /**
         * 校验码
         */
        private int verifyCode;
    }

    /**
     * 解析登录报文
     */
    public static Request decode(ByteBuf byteBuf) {
        int userId = byteBuf.readInt();
        String password = Jtt809Util.readGBKString(byteBuf, 8);
        String ip = Jtt809Util.readGBKString(byteBuf, 32);
        short port = byteBuf.readShort();
        return new Request(userId, password, ip, port);
    }

    /**
     * 编码登录回复报文
     */
    public static byte[] encode(Response response) {
        ByteBuf byteBuf = Unpooled.buffer(5);
        byteBuf.writeByte(response.getResult());
        byteBuf.writeInt(response.getVerifyCode());
        byte[] bytes = ByteBufUtil.getBytes(byteBuf);
        byteBuf.release();
        return bytes;
    }

}

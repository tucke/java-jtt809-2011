package org.tucke.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.tucke.gnsscenter.GnssCenterService;
import org.tucke.jtt809.common.CRC16CCITT;
import org.tucke.jtt809.common.Jtt809Constant;
import org.tucke.jtt809.common.Jtt809Util;
import org.tucke.jtt809.packet.common.OuterPacket;

import java.nio.charset.StandardCharsets;

public class PacketTest {

    public static void main(String[] args) {
        byte[] body = "这是测试的数据啊啊啊啊啊啊".getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = encode(body);
        String dump = ByteBufUtil.hexDump(buf);
        // 5b0000003c000000011001000025370102050000000000e8bf99e698afe6b58be8af95e79a84e695b0e68daee5958ae5958ae5958ae5958ae5958ae5958a44945d
        // 5b0000003c000000011001000025370102050000000000e8bf99e698afe6b58be8af95e79a84e695b0e68daee5958ae5958ae5958ae5958ae5958ae5958ab8535d
        System.out.println(dump);
        System.out.println();

        ByteBuf msg = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(dump, 0, dump.length() - 2));
        OuterPacket packet = decode(msg);
        System.out.println(packet);
    }

    public static ByteBuf encode(byte[] body) {
        ByteBuf out = Unpooled.buffer();

        // 24 = 头标识[1] + 数据头[22 = 长度[4] + 序列号[4] + 数据类型[2] + 接入码[4] + 版本号[3] + 加密标识[1] + 密钥[4]] + 尾标识[1]
        int len = body.length + 24;
        out.markReaderIndex();
        // 数据长度
        out.writeInt(len);
        // 序列号
        out.writeInt(2);
        // 业务数据类型
        out.writeShort(0x1001);
        // 下级平台接入码
        out.writeInt(9526);
        // 版本号
        String[] version = "v1.2.8".replace("v", "").split("\\.");
        for (String s : version) {
            out.writeByte(Byte.parseByte(s));
        }
        // 报文加密标识位
        out.writeByte(0);
        // 数据加密的密钥
        out.writeInt(0);
        // 数据体
        out.writeBytes(body);
        // 校验码
        byte[] crcBytes = new byte[out.readableBytes()];
        out.readBytes(crcBytes);
        out.writeShort(CRC16CCITT.crc16(crcBytes));

        // 转义
        out.resetReaderIndex();
        byte[] escapeBytes = new byte[out.readableBytes()];
        out.readBytes(escapeBytes);

        // 重置下标
        out.setIndex(0, 0);
        // 包头标识
        out.writeByte(Jtt809Constant.PACKET_HEAD_FLAG);
        // 数据内容
        out.writeBytes(Jtt809Util.escape(escapeBytes));
        // 包尾标识
        out.writeByte(Jtt809Constant.PACKET_END_FLAG);
        return out;
    }


    public static OuterPacket decode(ByteBuf msg) {
        if (msg.readByte() != Jtt809Constant.PACKET_HEAD_FLAG) {
            return null;
        }
        byte[] readableBytes = new byte[msg.readableBytes()];
        msg.readBytes(readableBytes);
        // 反转义处理
        byte[] bytes = Jtt809Util.unescape(readableBytes);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        // crc校验
        if (!Jtt809Util.validate(byteBuf)) {
            return null;
        }

        /* 解析外层包 */
        // 长度
        long length = byteBuf.readUnsignedInt();
        // 长度校验, 反转义之后数组加上包头和包尾长度与解析出来的长度对比；
        // 因为数据长度不包含校验码，而此时解析出来的数据不包含头尾标识，刚好都是2个字节，所以两个长度应该相等
        if (length != bytes.length) {
            return null;
        }
        // 报文序列号
        long sn = byteBuf.readUnsignedInt();
        // 业务数据类型
        int id = byteBuf.readUnsignedShort();
        // 下级平台接入码
        int gnsscenterId = byteBuf.readInt();
        // 协议版本号标识
        String version = "v" + byteBuf.readByte() + "." + byteBuf.readByte() + "." + byteBuf.readByte();
        // 报文加密标识位
        byte encryptFlag = byteBuf.readByte();
        // 数据加密解密的密匙
        long encryptKey = byteBuf.readUnsignedInt();
        // 消息体
        ByteBuf body;
        if (encryptFlag == 1) {
            byte[] encryptedBytes = new byte[byteBuf.readableBytes() - 2];
            byteBuf.readBytes(encryptedBytes);
            // 解密
            int[] param = GnssCenterService.getInstance().getDecryptParam(gnsscenterId);
            Jtt809Util.decrypt(param[0], param[1], param[2], encryptKey, encryptedBytes);
            body = Unpooled.wrappedBuffer(encryptedBytes);
        } else {
            body = byteBuf.readBytes(byteBuf.readableBytes() - 2);
        }
        // 校验码
        int crcCode = byteBuf.readUnsignedShort();
        ReferenceCountUtil.release(byteBuf);
        return new OuterPacket(length, sn, id, gnsscenterId, version, encryptFlag, encryptKey, body, crcCode);
    }

}
